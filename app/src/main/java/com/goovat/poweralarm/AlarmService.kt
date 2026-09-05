package com.goovat.poweralarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class AlarmService : Service() {

    private lateinit var powerMonitor: PowerMonitor
    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var settingsStore: AlarmSettingsStore
    private lateinit var eventEvaluator: AlarmEventEvaluator
    private lateinit var alarmEngine: AlarmEngine

    private val handler = Handler(Looper.getMainLooper())

    private var previousPower: PowerSnapshot? = null
    private var previousBattery: BatterySnapshot? = null

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkState()
            handler.postDelayed(this, MONITOR_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()

        powerMonitor = PowerMonitor(this)
        batteryMonitor = BatteryMonitor(this)
        settingsStore = AlarmSettingsStore(this)
        eventEvaluator = AlarmEventEvaluator()
        alarmEngine = AlarmEngine(this)

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification("Monitoring power and battery")
        )

        previousPower = powerMonitor.getCurrentState()
        previousBattery = batteryMonitor.getCurrentState()

        handler.post(monitorRunnable)
    }

    private fun checkState() {
        val currentPower = powerMonitor.getCurrentState()
        val currentBattery = batteryMonitor.getCurrentState()
        val settings = settingsStore.load()

        val oldPower = previousPower
        val oldBattery = previousBattery

        if (oldPower != null) {
            val events = eventEvaluator.evaluatePowerEvents(
                previous = oldPower,
                current = currentPower,
                settings = settings
            )

            events.forEach(::handleEvent)
        }

        if (oldBattery != null) {
            val events = eventEvaluator.evaluateBatteryEvents(
                previous = oldBattery,
                current = currentBattery,
                settings = settings
            )

            events.forEach(::handleEvent)
        }

        previousPower = currentPower
        previousBattery = currentBattery

        updateNotification(currentBattery, currentPower)
    }

    private fun handleEvent(event: AlarmEvent) {
        Log.i(TAG, "Alarm event detected: $event")

        alarmEngine.trigger(event)
    }

    private fun updateNotification(
        battery: BatterySnapshot,
        power: PowerSnapshot
    ) {
        val powerText = if (power.isExternalPowerConnected) {
            "Power connected"
        } else {
            "Power disconnected"
        }

        val notification = buildNotification(
            "$powerText • Battery ${battery.percentage}%"
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(content: String): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Power Alarm")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Power monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Power Alarm background monitoring"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        alarmEngine.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "PowerAlarm"
        private const val CHANNEL_ID = "power_monitoring"
        private const val NOTIFICATION_ID = 1001
        private const val MONITOR_INTERVAL_MS = 5_000L
    }
}
