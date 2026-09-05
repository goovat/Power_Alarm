package com.goovat.poweralarm

import android.content.Context
import android.content.Intent
import android.os.BatteryManager

data class PowerSnapshot(
    val isCharging: Boolean,
    val isExternalPowerConnected: Boolean,
    val plugType: Int
)

class PowerMonitor(private val context: Context) {

    fun getCurrentState(): PowerSnapshot {
        val intent = context.registerReceiver(
            null,
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val status = intent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        val plugType = intent?.getIntExtra(
            BatteryManager.EXTRA_PLUGGED,
            0
        ) ?: 0

        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        return PowerSnapshot(
            isCharging = isCharging,
            isExternalPowerConnected = plugType != 0,
            plugType = plugType
        )
    }
}
