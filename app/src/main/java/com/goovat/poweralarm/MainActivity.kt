package com.goovat.poweralarm

import android.os.Bundle
import android.os.BatteryManager
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val batteryMonitor = BatteryMonitor(this)
        val powerMonitor = PowerMonitor(this)

        val battery = batteryMonitor.getCurrentState()
        val power = powerMonitor.getCurrentState()

        val chargingText = when (battery.status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }

        val powerText = when {
            power.plugType != 0 -> "External power connected"
            else -> "External power disconnected"
        }

        val view = TextView(this).apply {
            text = """
                Power Alarm

                Battery: ${battery.percentage}%
                Status: $chargingText
                Temperature: ${battery.temperatureCelsius}°C

                Power: $powerText
            """.trimIndent()

            textSize = 22f
            setPadding(48, 48, 48, 48)
        }

        setContentView(view)
    }
}
