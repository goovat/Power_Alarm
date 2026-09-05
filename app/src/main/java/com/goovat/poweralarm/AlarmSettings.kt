package com.goovat.poweralarm

import android.content.Context

data class AlarmSettings(
    val lowBatteryEnabled: Boolean = true,
    val lowBatteryThreshold: Int = 20,
    val criticalBatteryEnabled: Boolean = true,
    val criticalBatteryThreshold: Int = 10,
    val fullBatteryEnabled: Boolean = true,
    val fullBatteryThreshold: Int = 100,
    val powerOffEnabled: Boolean = true,
    val powerRestoredEnabled: Boolean = true,
    val chargingStartedEnabled: Boolean = false,
    val chargingStoppedEnabled: Boolean = false
)

class AlarmSettingsStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "power_alarm_settings",
        Context.MODE_PRIVATE
    )

    fun load(): AlarmSettings {
        return AlarmSettings(
            lowBatteryEnabled = preferences.getBoolean(
                KEY_LOW_ENABLED,
                true
            ),
            lowBatteryThreshold = preferences.getInt(
                KEY_LOW_THRESHOLD,
                20
            ),
            criticalBatteryEnabled = preferences.getBoolean(
                KEY_CRITICAL_ENABLED,
                true
            ),
            criticalBatteryThreshold = preferences.getInt(
                KEY_CRITICAL_THRESHOLD,
                10
            ),
            fullBatteryEnabled = preferences.getBoolean(
                KEY_FULL_ENABLED,
                true
            ),
            fullBatteryThreshold = preferences.getInt(
                KEY_FULL_THRESHOLD,
                100
            ),
            powerOffEnabled = preferences.getBoolean(
                KEY_POWER_OFF_ENABLED,
                true
            ),
            powerRestoredEnabled = preferences.getBoolean(
                KEY_POWER_RESTORED_ENABLED,
                true
            ),
            chargingStartedEnabled = preferences.getBoolean(
                KEY_CHARGING_STARTED_ENABLED,
                false
            ),
            chargingStoppedEnabled = preferences.getBoolean(
                KEY_CHARGING_STOPPED_ENABLED,
                false
            )
        )
    }

    fun save(settings: AlarmSettings) {
        preferences.edit()
            .putBoolean(KEY_LOW_ENABLED, settings.lowBatteryEnabled)
            .putInt(KEY_LOW_THRESHOLD, settings.lowBatteryThreshold.coerceIn(1, 100))
            .putBoolean(KEY_CRITICAL_ENABLED, settings.criticalBatteryEnabled)
            .putInt(KEY_CRITICAL_THRESHOLD, settings.criticalBatteryThreshold.coerceIn(1, 100))
            .putBoolean(KEY_FULL_ENABLED, settings.fullBatteryEnabled)
            .putInt(KEY_FULL_THRESHOLD, settings.fullBatteryThreshold.coerceIn(1, 100))
            .putBoolean(KEY_POWER_OFF_ENABLED, settings.powerOffEnabled)
            .putBoolean(KEY_POWER_RESTORED_ENABLED, settings.powerRestoredEnabled)
            .putBoolean(KEY_CHARGING_STARTED_ENABLED, settings.chargingStartedEnabled)
            .putBoolean(KEY_CHARGING_STOPPED_ENABLED, settings.chargingStoppedEnabled)
            .apply()
    }

    companion object {
        private const val KEY_LOW_ENABLED = "low_battery_enabled"
        private const val KEY_LOW_THRESHOLD = "low_battery_threshold"
        private const val KEY_CRITICAL_ENABLED = "critical_battery_enabled"
        private const val KEY_CRITICAL_THRESHOLD = "critical_battery_threshold"
        private const val KEY_FULL_ENABLED = "full_battery_enabled"
        private const val KEY_FULL_THRESHOLD = "full_battery_threshold"
        private const val KEY_POWER_OFF_ENABLED = "power_off_enabled"
        private const val KEY_POWER_RESTORED_ENABLED = "power_restored_enabled"
        private const val KEY_CHARGING_STARTED_ENABLED = "charging_started_enabled"
        private const val KEY_CHARGING_STOPPED_ENABLED = "charging_stopped_enabled"
    }
}
