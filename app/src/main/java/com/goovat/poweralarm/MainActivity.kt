package com.goovat.poweralarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.BatteryManager
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var monitoringStatusText: TextView
    private lateinit var monitoringCard: LinearLayout
    private lateinit var batteryValueText: TextView
    private lateinit var batteryDetailText: TextView
    private lateinit var powerValueText: TextView
    private lateinit var powerDetailText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

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
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(246, 248, 252))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 36, 24, 36)
            background = roundedBackground(
                Color.rgb(25, 103, 210),
                28f
            )
        }

        header.addView(TextView(this).apply {
            text = "⚡"
            textSize = 42f
            gravity = Gravity.CENTER
        })

        header.addView(TextView(this).apply {
            text = "Power Alarm"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })

        header.addView(TextView(this).apply {
            text = "Power & battery monitoring"
            textSize = 16f
            setTextColor(Color.WHITE)
            alpha = 0.9f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        })

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
        )

        root.addView(sectionLabel("MONITORING"))

        monitoringCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        monitoringStatusText = TextView(this).apply {
            textSize = 22f
            gravity = Gravity.CENTER
        }

        monitoringCard.addView(monitoringStatusText)

        root.addView(
            monitoringCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
        )

        updateMonitoringStatus()

        root.addView(sectionLabel("BATTERY"))

        val batteryCard = infoCard(
            title = "🔋 Battery",
            value = "${battery.percentage}%",
            detail = "$chargingText  •  ${battery.temperatureCelsius}°C"
        )

        batteryValueText = batteryCard.getChildAt(1) as TextView
        batteryDetailText = batteryCard.getChildAt(2) as TextView

        root.addView(
            batteryCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        )

        root.addView(sectionLabel("POWER SUPPLY"))

        val powerCard = infoCard(
            title = "⚡ External Power",
            value = if (power.isExternalPowerConnected) {
                "Connected"
            } else {
                "Disconnected"
            },
            detail = if (power.isCharging) {
                "Phone is receiving power"
            } else {
                "Phone is not charging"
            }
        )

        powerValueText = powerCard.getChildAt(1) as TextView
        powerDetailText = powerCard.getChildAt(2) as TextView

        root.addView(
            powerCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 28
            }
        )

        val startButton = actionButton(
            text = "▶  Start Monitoring",
            backgroundColor = Color.rgb(46, 125, 50)
        ) {
            val intent = Intent(
                this@MainActivity,
                AlarmService::class.java
            )

            ContextCompat.startForegroundService(
                this@MainActivity,
                intent
            )

            updateMonitoringStatus()
        }

        root.addView(
            startButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60.dp()
            ).apply {
                bottomMargin = 12
            }
        )

        val stopButton = actionButton(
            text = "■  Stop Monitoring",
            backgroundColor = Color.rgb(198, 40, 40)
        ) {
            AlarmService.stopMonitoring(this@MainActivity)
            updateMonitoringStatus()
        }

        root.addView(
            stopButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60.dp()
            ).apply {
                bottomMargin = 12
            }
        )

        val settingsButton = actionButton(
            text = "⚙  Settings",
            backgroundColor = Color.rgb(94, 53, 177)
        ) {
            startActivity(
                Intent(
                    this@MainActivity,
                    SettingsActivity::class.java
                )
            )
        }

        root.addView(
            settingsButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60.dp()
            )
        )

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()

        if (::monitoringStatusText.isInitialized) {
            updateMonitoringStatus()
            updateCurrentState()
        }
    }

    private fun updateCurrentState() {
        val battery = BatteryMonitor(this).getCurrentState()
        val power = PowerMonitor(this).getCurrentState()

        val chargingText = when (battery.status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }

        batteryValueText.text = "${battery.percentage}%"
        batteryDetailText.text =
            "$chargingText  •  ${battery.temperatureCelsius}°C"

        powerValueText.text = if (power.isExternalPowerConnected) {
            "Connected"
        } else {
            "Disconnected"
        }

        powerDetailText.text = if (power.isCharging) {
            "Phone is receiving power"
        } else {
            "Phone is not charging"
        }
    }

    private fun updateMonitoringStatus() {
        val monitoring = AlarmService.isMonitoring

        monitoringStatusText.text = if (monitoring) {
            "●  Monitoring is ON"
        } else {
            "○  Monitoring is OFF"
        }

        monitoringStatusText.setTextColor(
            if (monitoring) {
                Color.rgb(46, 125, 50)
            } else {
                Color.rgb(117, 117, 117)
            }
        )

        monitoringCard.background = roundedBackground(
            if (monitoring) {
                Color.rgb(232, 245, 233)
            } else {
                Color.rgb(238, 240, 244)
            },
            24f
        )
    }

    private fun sectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.rgb(90, 100, 115))
            setPadding(8, 0, 8, 10)
        }
    }

    private fun infoCard(
        title: String,
        value: String,
        detail: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            background = roundedBackground(
                Color.WHITE,
                24f
            )

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 17f
                setTextColor(Color.rgb(55, 65, 81))
            })

            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 28f
                setTextColor(Color.rgb(25, 103, 210))
                setPadding(0, 8, 0, 4)
            })

            addView(TextView(this@MainActivity).apply {
                text = detail
                textSize = 15f
                setTextColor(Color.rgb(100, 110, 125))
            })
        }
    }

    private fun actionButton(
        text: String,
        backgroundColor: Int,
        onClick: () -> Unit
    ): Button {
        return Button(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = roundedBackground(
                backgroundColor,
                18f
            )
            setOnClickListener {
                onClick()
            }
        }
    }

    private fun roundedBackground(
        color: Int,
        radiusDp: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp.dp()
        }
    }

    private fun Float.dp(): Float {
        return this * resources.displayMetrics.density
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 2001
    }
}
