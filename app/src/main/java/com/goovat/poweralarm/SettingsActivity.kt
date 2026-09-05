package com.goovat.poweralarm

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsStore: AlarmSettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsStore = AlarmSettingsStore(this)
        val settings = settingsStore.load()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        root.addView(TextView(this).apply {
            text = "Power Alarm Settings"
            textSize = 26f
        })

        root.addView(sectionTitle("Battery"))

        val lowEnabled = checkBox("Low battery alert", settings.lowBatteryEnabled)
        val lowThreshold = thresholdInput(
            settings.lowBatteryThreshold,
            "Low battery threshold (%)"
        )

        val criticalEnabled = checkBox(
            "Critical battery alert",
            settings.criticalBatteryEnabled
        )
        val criticalThreshold = thresholdInput(
            settings.criticalBatteryThreshold,
            "Critical battery threshold (%)"
        )

        val fullEnabled = checkBox(
            "Full battery alert",
            settings.fullBatteryEnabled
        )
        val fullThreshold = thresholdInput(
            settings.fullBatteryThreshold,
            "Full battery threshold (%)"
        )

        root.addView(lowEnabled)
        root.addView(lowThreshold)
        root.addView(criticalEnabled)
        root.addView(criticalThreshold)
        root.addView(fullEnabled)
        root.addView(fullThreshold)

        root.addView(sectionTitle("Power"))

        val powerOffEnabled = checkBox(
            "Power OFF alert",
            settings.powerOffEnabled
        )
        val powerRestoredEnabled = checkBox(
            "Power restored alert",
            settings.powerRestoredEnabled
        )

        root.addView(powerOffEnabled)
        root.addView(powerRestoredEnabled)

        root.addView(sectionTitle("Charging"))

        val chargingStartedEnabled = checkBox(
            "Charging started alert",
            settings.chargingStartedEnabled
        )
        val chargingStoppedEnabled = checkBox(
            "Charging stopped alert",
            settings.chargingStoppedEnabled
        )

        root.addView(chargingStartedEnabled)
        root.addView(chargingStoppedEnabled)

        root.addView(Button(this).apply {
            text = "Save Settings"
            setOnClickListener {
                val updated = AlarmSettings(
                    lowBatteryEnabled = lowEnabled.isChecked,
                    lowBatteryThreshold = readThreshold(lowThreshold),
                    criticalBatteryEnabled = criticalEnabled.isChecked,
                    criticalBatteryThreshold = readThreshold(criticalThreshold),
                    fullBatteryEnabled = fullEnabled.isChecked,
                    fullBatteryThreshold = readThreshold(fullThreshold),
                    powerOffEnabled = powerOffEnabled.isChecked,
                    powerRestoredEnabled = powerRestoredEnabled.isChecked,
                    chargingStartedEnabled = chargingStartedEnabled.isChecked,
                    chargingStoppedEnabled = chargingStoppedEnabled.isChecked
                )

                settingsStore.save(updated)
                finish()
            }
        })

        setContentView(root)
    }

    private fun sectionTitle(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 20f
            setPadding(0, 32, 0, 8)
        }
    }

    private fun checkBox(
        label: String,
        checked: Boolean
    ): CheckBox {
        return CheckBox(this).apply {
            text = label
            isChecked = checked
        }
    }

    private fun thresholdInput(
        value: Int,
        hint: String
    ): EditText {
        return EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            this.hint = hint
            setText(value.toString())
        }
    }

    private fun readThreshold(input: EditText): Int {
        return input.text
            .toString()
            .toIntOrNull()
            ?.coerceIn(1, 100)
            ?: 50
    }
}
