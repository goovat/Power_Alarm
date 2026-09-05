package com.goovat.poweralarm

import android.content.Intent
import android.os.Bundle
import android.os.BatteryManager
import android.widget.Button
import android.widget.LinearLayout
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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "Power Alarm"
            textSize = 28f
        })

        root.addView(TextView(this).apply {
            text = """
                Battery: ${battery.percentage}%
                Status: $chargingText
                Temperature: ${battery.temperatureCelsius}°C

                Power: ${
                    if (power.isExternalPowerConnected) {
                        "External power connected"
                    } else {
                        "External power disconnected"
                    }
                }
            """.trimIndent()
            textSize = 20f
            setPadding(0, 32, 0, 32)
        })

        root.addView(Button(this).apply {
            text = "Settings"
            setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, SettingsActivity::class.java)
                )
            }
        })

        setContentView(root)
    }
}
