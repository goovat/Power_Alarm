package com.goovat.poweralarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

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

        isActive = true

        startSound()
        startVibration()
    }

    private fun startSound() {
        val uri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_ALARM
        ) ?: RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION
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
            context.getSystemService(Context.VIBRATOR_SERVICE)
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
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        isActive = false
    }
}
