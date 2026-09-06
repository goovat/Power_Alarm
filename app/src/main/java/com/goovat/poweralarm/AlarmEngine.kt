package com.goovat.poweralarm

import android.app.KeyguardManager
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

    private val oneShotPlayers = mutableSetOf<MediaPlayer>()

    var isActive: Boolean = false
        private set

    fun trigger(event: AlarmEvent) {
        if (isActive) {
            return
        }

        try {
            when (event) {
                AlarmEvent.PowerOff -> triggerPowerOff()
                AlarmEvent.PowerRestored,
                AlarmEvent.ChargingStarted,
                AlarmEvent.ChargingStopped -> {
                    playOnce(
                        soundUri = loadPowerSupplySoundUri()
                    )
                }

                is AlarmEvent.LowBattery,
                is AlarmEvent.CriticalBattery,
                is AlarmEvent.FullBattery -> {
                    playOnce(
                        soundUri = loadBatterySoundUri()
                    )
                }
            }
        } catch (error: Exception) {
            Log.e(
                TAG,
                "Failed to trigger event: $event",
                error
            )

            release()
        }
    }

    private fun triggerPowerOff() {
        val keyguardManager = context.getSystemService(
            KeyguardManager::class.java
        )

        val isLocked = keyguardManager?.isKeyguardLocked == true

        if (isLocked) {
            startProtectedPowerOffAlarm()
        } else {
            playOnce(
                soundUri = loadPowerOffUnlockedSoundUri()
            )
        }
    }

    private fun startProtectedPowerOffAlarm() {
        startSound(
            soundUri = loadPowerOffLockedSoundUri(),
            looping = true
        )

        startVibration()

        isActive = true

        Log.i(
            TAG,
            "Protected Power OFF alarm activated"
        )
    }

    private fun playOnce(soundUri: Uri) {
        val player = MediaPlayer().apply {
            setDataSource(context, soundUri)

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_ALARM
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            )

            setOnCompletionListener { completedPlayer ->
                synchronized(oneShotPlayers) {
                    oneShotPlayers.remove(completedPlayer)
                }

                completedPlayer.release()
            }

            setOnErrorListener { failedPlayer, _, _ ->
                synchronized(oneShotPlayers) {
                    oneShotPlayers.remove(failedPlayer)
                }

                failedPlayer.release()
                true
            }

            prepare()
        }

        synchronized(oneShotPlayers) {
            oneShotPlayers.add(player)
        }

        player.start()

        Log.i(
            TAG,
            "One-shot alert sound started"
        )
    }

    private fun startSound(
        soundUri: Uri,
        looping: Boolean
    ) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, soundUri)

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_ALARM
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            )

            isLooping = looping
            prepare()
            start()
        }
    }

    private fun loadBatterySoundUri(): Uri {
        val settings = AlarmSettingsStore(context).load()

        return selectedUriOrDefault(
            settings.batteryAlertSoundUri
        )
    }

    private fun loadPowerSupplySoundUri(): Uri {
        val settings = AlarmSettingsStore(context).load()

        return selectedUriOrDefault(
            settings.powerSupplyAlertSoundUri
        )
    }

    private fun loadPowerOffLockedSoundUri(): Uri {
        val settings = AlarmSettingsStore(context).load()

        return selectedUriOrDefault(
            settings.powerOffLockedSoundUri
        )
    }

    private fun loadPowerOffUnlockedSoundUri(): Uri {
        val settings = AlarmSettingsStore(context).load()

        return selectedUriOrDefault(
            settings.powerOffUnlockedSoundUri
        )
    }

    private fun selectedUriOrDefault(
        storedUri: String?
    ): Uri {
        val selectedUri = storedUri
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)

        return selectedUri
            ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            )
            ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )
            ?: throw IllegalStateException(
                "No alarm or notification ringtone available"
            )
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

        synchronized(oneShotPlayers) {
            oneShotPlayers.forEach { player ->
                try {
                    player.stop()
                } catch (_: IllegalStateException) {
                }

                player.release()
            }

            oneShotPlayers.clear()
        }

        vibrator?.cancel()
        vibrator = null

        isActive = false
    }

    companion object {
        private const val TAG = "PowerAlarmEngine"
    }
}
