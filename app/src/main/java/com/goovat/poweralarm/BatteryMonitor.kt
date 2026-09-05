package com.goovat.poweralarm

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class BatterySnapshot(
    val percentage: Int,
    val status: Int,
    val temperatureCelsius: Float
)

class BatteryMonitor(private val context: Context) {

    fun getCurrentState(): BatterySnapshot {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = intent?.getIntExtra(
            BatteryManager.EXTRA_LEVEL,
            0
        ) ?: 0

        val scale = intent?.getIntExtra(
            BatteryManager.EXTRA_SCALE,
            100
        ) ?: 100

        val temperature = intent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            0
        ) ?: 0

        val percentage = if (scale > 0) {
            (level * 100) / scale
        } else {
            0
        }

        return BatterySnapshot(
            percentage = percentage,
            status = intent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN,
            temperatureCelsius = temperature / 10f
        )
    }
}
