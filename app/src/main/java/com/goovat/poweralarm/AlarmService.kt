package com.goovat.poweralarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.util.UUID

class AlarmService : Service() {

    private lateinit var powerMonitor: PowerMonitor
    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var settingsStore: AlarmSettingsStore
    private lateinit var eventEvaluator: AlarmEventEvaluator
    private lateinit var alarmEngine: AlarmEngine

    private val handler = Handler(Looper.getMainLooper())

    private var previousPower: PowerSnapshot? = null
    private var previousBattery: BatterySnapshot? = null

    private var activeAlarmSessionToken: String? = null

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

        createNotificationChannels()

        startForeground(
            NOTIFICATION_ID,
            buildMonitoringNotification("Monitoring power and battery")
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

        updateMonitoringNotification(
            currentBattery,
            currentPower
        )
    }

    private fun handleEvent(event: AlarmEvent) {
        Log.i(TAG, "Alarm event detected: $event")

        val wasActive = alarmEngine.isActive

        alarmEngine.trigger(event)

        if (!wasActive && alarmEngine.isActive) {
            activeAlarmSessionToken = UUID.randomUUID().toString()
            showAlarmNotification(event)
        }
    }

    private fun updateMonitoringNotification(
        battery: BatterySnapshot,
        power: PowerSnapshot
    ) {
        if (alarmEngine.isActive) {
            return
        }

        val powerText = if (power.isExternalPowerConnected) {
            "Power connected"
        } else {
            "Power disconnected"
        }

        val notification = buildMonitoringNotification(
            "$powerText • Battery ${battery.percentage}%"
        )

        val manager = getSystemService(
            NotificationManager::class.java
        )

        manager.notify(
            NOTIFICATION_ID,
            notification
        )
    }

    private fun showAlarmNotification(event: AlarmEvent) {
        val sessionToken = activeAlarmSessionToken
            ?: return

        val intent = Intent(
            this,
            AlarmActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra(
                EXTRA_ALARM_SESSION_TOKEN,
                sessionToken
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(
            this,
            ALARM_CHANNEL_ID
        )
            .setContentTitle("Power Alarm")
            .setContentText(eventDescription(event))
            .setSmallIcon(
                android.R.drawable.ic_lock_idle_charging
            )
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(
                pendingIntent,
                true
            )
            .build()

        val manager = getSystemService(
            NotificationManager::class.java
        )

        manager.notify(
            ALARM_NOTIFICATION_ID,
            notification
        )
    }

    private fun eventDescription(event: AlarmEvent): String {
        return when (event) {
            AlarmEvent.PowerOff ->
                "External power disconnected"

            AlarmEvent.PowerRestored ->
                "External power restored"

            AlarmEvent.ChargingStarted ->
                "Charging started"

            AlarmEvent.ChargingStopped ->
                "Charging stopped"

            is AlarmEvent.LowBattery ->
                "Battery low: ${event.percentage}%"

            is AlarmEvent.CriticalBattery ->
                "Battery critical: ${event.percentage}%"

            is AlarmEvent.FullBattery ->
                "Battery full: ${event.percentage}%"
        }
    }

    private fun buildMonitoringNotification(
        content: String
    ): Notification {
        return Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("Power Alarm")
            .setContentText(content)
            .setSmallIcon(
                android.R.drawable.ic_lock_idle_charging
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        val monitoringChannel = NotificationChannel(
            CHANNEL_ID,
            "Power monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description =
                "Power Alarm background monitoring"
        }

        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Power alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                "Critical Power Alarm alerts"
            setBypassDnd(true)
            enableVibration(true)
        }

        val manager = getSystemService(
            NotificationManager::class.java
        )

        manager.createNotificationChannel(
            monitoringChannel
        )

        manager.createNotificationChannel(
            alarmChannel
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            val suppliedToken =
                intent.getStringExtra(EXTRA_ALARM_SESSION_TOKEN)

            if (
                suppliedToken == null ||
                suppliedToken != activeAlarmSessionToken ||
                !alarmEngine.isActive
            ) {
                Log.w(
                    TAG,
                    "Rejected unauthorized alarm stop request"
                )

                return START_STICKY
            }

            alarmEngine.release()
            activeAlarmSessionToken = null

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.cancel(ALARM_NOTIFICATION_ID)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        alarmEngine.release()
        activeAlarmSessionToken = null

        val manager = getSystemService(
            NotificationManager::class.java
        )

        manager.cancel(ALARM_NOTIFICATION_ID)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "PowerAlarm"

        private const val CHANNEL_ID =
            "power_monitoring"

        private const val ALARM_CHANNEL_ID =
            "power_alarm"

        private const val NOTIFICATION_ID =
            1001

        private const val ALARM_NOTIFICATION_ID =
            1002

        private const val ALARM_REQUEST_CODE =
            2002

        private const val MONITOR_INTERVAL_MS =
            5_000L

        private const val ACTION_STOP_ALARM =
            "com.goovat.poweralarm.action.STOP_ALARM"

        const val EXTRA_ALARM_SESSION_TOKEN =
            "com.goovat.poweralarm.extra.ALARM_SESSION_TOKEN"

        fun stopAlarm(
            context: Context,
            sessionToken: String
        ) {
            val intent = Intent(
                context,
                AlarmService::class.java
            ).apply {
                action = ACTION_STOP_ALARM

                putExtra(
                    EXTRA_ALARM_SESSION_TOKEN,
                    sessionToken
                )
            }

            context.startService(intent)
        }
    }
}
