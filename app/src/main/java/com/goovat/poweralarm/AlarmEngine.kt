package com.goovat.poweralarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class AlarmEngine(
    private val context: Context
) {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    var isActive: Boolean = false
        private set

    fun trigger(event: AlarmEvent) {
        if (isActive) {
            return
        }

        try {
            startSound()
            startVibration()
            isActive = true

            Log.i(TAG, "Alarm activated for event: $event")
        } catch (error: Exception) {
            Log.e(
                TAG,
                "Failed to activate alarm for event: $event",
                error
            )

            release()
        }
    }

    private fun startSound() {
        val settings = AlarmSettingsStore(context).load()

        val selectedUri = settings.powerOffLockedSoundUri
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)

        val uri = selectedUri
            ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            )
            ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )
            ?: throw IllegalStateException(
                "No alarm or notification ringtone available"
            )

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            )

            isLooping = true
            prepare()
            start()
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(
                VibratorManager::class.java
            )
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(
                Context.VIBRATOR_SERVICE
            ) as? Vibrator
        }

        val pattern = longArrayOf(
            0L,
            800L,
            400L,
            800L,
            400L
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    pattern,
                    0
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    fun release() {
        try {
            mediaPlayer?.stop()
        } catch (_: IllegalStateException) {
        }

        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        isActive = false
    }

    companion object {
        private const val TAG = "PowerAlarmEngine"
    }
}
