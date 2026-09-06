package com.goovat.poweralarm

import android.content.Context

data class PersistedAlarmSession(
    val token: String,
    val event: AlarmEvent
)

class AlarmSessionStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "power_alarm_session",
        Context.MODE_PRIVATE
    )

    fun save(
        token: String,
        event: AlarmEvent
    ) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EVENT_TYPE, eventType(event))
            .putInt(
                KEY_EVENT_PERCENTAGE,
                eventPercentage(event)
            )
            .apply()
    }

    fun load(): PersistedAlarmSession? {
        val token = preferences.getString(
            KEY_TOKEN,
            null
        ) ?: return null

        val eventType = preferences.getString(
            KEY_EVENT_TYPE,
            null
        ) ?: return null

        val percentage = preferences.getInt(
            KEY_EVENT_PERCENTAGE,
            0
        )

        val event = eventFromStoredValues(
            eventType,
            percentage
        ) ?: return null

        return PersistedAlarmSession(
            token = token,
            event = event
        )
    }

    fun clear() {
        preferences.edit()
            .clear()
            .apply()
    }

    private fun eventType(event: AlarmEvent): String {
        return when (event) {
            AlarmEvent.PowerOff ->
                EVENT_POWER_OFF

            AlarmEvent.PowerRestored ->
                EVENT_POWER_RESTORED

            AlarmEvent.ChargingStarted ->
                EVENT_CHARGING_STARTED

            AlarmEvent.ChargingStopped ->
                EVENT_CHARGING_STOPPED

            is AlarmEvent.LowBattery ->
                EVENT_LOW_BATTERY

            is AlarmEvent.CriticalBattery ->
                EVENT_CRITICAL_BATTERY

            is AlarmEvent.FullBattery ->
                EVENT_FULL_BATTERY
        }
    }

    private fun eventPercentage(event: AlarmEvent): Int {
        return when (event) {
            is AlarmEvent.LowBattery ->
                event.percentage

            is AlarmEvent.CriticalBattery ->
                event.percentage

            is AlarmEvent.FullBattery ->
                event.percentage

            else ->
                0
        }
    }

    private fun eventFromStoredValues(
        type: String,
        percentage: Int
    ): AlarmEvent? {
        return when (type) {
            EVENT_POWER_OFF ->
                AlarmEvent.PowerOff

            EVENT_POWER_RESTORED ->
                AlarmEvent.PowerRestored

            EVENT_CHARGING_STARTED ->
                AlarmEvent.ChargingStarted

            EVENT_CHARGING_STOPPED ->
                AlarmEvent.ChargingStopped

            EVENT_LOW_BATTERY ->
                AlarmEvent.LowBattery(percentage)

            EVENT_CRITICAL_BATTERY ->
                AlarmEvent.CriticalBattery(percentage)

            EVENT_FULL_BATTERY ->
                AlarmEvent.FullBattery(percentage)

            else ->
                null
        }
    }

    companion object {
        private const val KEY_TOKEN = "active_alarm_token"
        private const val KEY_EVENT_TYPE = "active_alarm_event"
        private const val KEY_EVENT_PERCENTAGE =
            "active_alarm_percentage"

        private const val EVENT_POWER_OFF = "power_off"
        private const val EVENT_POWER_RESTORED = "power_restored"
        private const val EVENT_CHARGING_STARTED =
            "charging_started"
        private const val EVENT_CHARGING_STOPPED =
            "charging_stopped"
        private const val EVENT_LOW_BATTERY = "low_battery"
        private const val EVENT_CRITICAL_BATTERY =
            "critical_battery"
        private const val EVENT_FULL_BATTERY = "full_battery"
    }
}
