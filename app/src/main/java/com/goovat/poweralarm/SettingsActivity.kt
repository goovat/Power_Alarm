package com.goovat.poweralarm

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsStore: AlarmSettingsStore

    private var selectedBatteryAlertSoundUri: String? = null
    private var selectedPowerSupplyAlertSoundUri: String? = null
    private var selectedPowerOffLockedSoundUri: String? = null
    private var selectedPowerOffUnlockedSoundUri: String? = null

    private lateinit var batterySoundButton: Button
    private lateinit var powerSupplySoundButton: Button
    private lateinit var powerOffLockedSoundButton: Button
    private lateinit var powerOffUnlockedSoundButton: Button

    private var pendingSoundSelection: SoundSelection? = null

    companion object {
        private const val REQUEST_SOUND = 3001
    }

    private enum class SoundSelection {
        BATTERY,
        POWER_SUPPLY,
        POWER_OFF_LOCKED,
        POWER_OFF_UNLOCKED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsStore = AlarmSettingsStore(this)
        val settings = settingsStore.load()

        selectedBatteryAlertSoundUri =
            settings.batteryAlertSoundUri

        selectedPowerSupplyAlertSoundUri =
            settings.powerSupplyAlertSoundUri

        selectedPowerOffLockedSoundUri =
            settings.powerOffLockedSoundUri

        selectedPowerOffUnlockedSoundUri =
            settings.powerOffUnlockedSoundUri

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 20.dp(), 20.dp(), 28.dp())
            setBackgroundColor(Color.rgb(246, 248, 252))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20.dp(), 18.dp(), 20.dp(), 18.dp())
            background = roundedBackground(
                Color.rgb(25, 103, 210),
                20.dp().toFloat()
            )
        }

        header.addView(TextView(this).apply {
            text = "⚙️  Power Alarm Settings"
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        })

        header.addView(TextView(this).apply {
            text = "Customize alerts, sounds and battery thresholds"
            textSize = 14f
            setTextColor(Color.rgb(225, 238, 255))
            setPadding(0, 6.dp(), 0, 0)
        })

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 18.dp()
            }
        )

        root.addView(sectionTitle("Battery"))

        val lowEnabled = checkBox(
            "Low battery alert",
            settings.lowBatteryEnabled
        )

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

        batterySoundButton = soundButton(
            "Battery alert sound",
            selectedBatteryAlertSoundUri,
            SoundSelection.BATTERY
        )

        root.addView(batterySoundButton)

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

        powerOffLockedSoundButton = soundButton(
            "Power OFF sound — Locked",
            selectedPowerOffLockedSoundUri,
            SoundSelection.POWER_OFF_LOCKED
        )

        root.addView(powerOffLockedSoundButton)

        powerOffUnlockedSoundButton = soundButton(
            "Power OFF sound — Unlocked",
            selectedPowerOffUnlockedSoundUri,
            SoundSelection.POWER_OFF_UNLOCKED
        )

        root.addView(powerOffUnlockedSoundButton)

        root.addView(powerRestoredEnabled)

        powerSupplySoundButton = soundButton(
            "Power supply alert sound",
            selectedPowerSupplyAlertSoundUri,
            SoundSelection.POWER_SUPPLY
        )

        root.addView(powerSupplySoundButton)

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
            text = "💾  Save Settings"
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setAllCaps(false)
            background = roundedBackground(
                Color.rgb(46, 125, 50),
                16.dp().toFloat()
            )
            elevation = 4.dp().toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56.dp()
            ).apply {
                topMargin = 12.dp()
            }

            setOnClickListener {
                val updated = AlarmSettings(
                    batteryAlertSoundUri =
                        selectedBatteryAlertSoundUri,

                    powerSupplyAlertSoundUri =
                        selectedPowerSupplyAlertSoundUri,

                    powerOffLockedSoundUri =
                        selectedPowerOffLockedSoundUri,

                    powerOffUnlockedSoundUri =
                        selectedPowerOffUnlockedSoundUri,

                    lowBatteryEnabled =
                        lowEnabled.isChecked,

                    lowBatteryThreshold =
                        readThreshold(lowThreshold),

                    criticalBatteryEnabled =
                        criticalEnabled.isChecked,

                    criticalBatteryThreshold =
                        readThreshold(criticalThreshold),

                    fullBatteryEnabled =
                        fullEnabled.isChecked,

                    fullBatteryThreshold =
                        readThreshold(fullThreshold),

                    powerOffEnabled =
                        powerOffEnabled.isChecked,

                    powerRestoredEnabled =
                        powerRestoredEnabled.isChecked,

                    chargingStartedEnabled =
                        chargingStartedEnabled.isChecked,

                    chargingStoppedEnabled =
                        chargingStoppedEnabled.isChecked
                )

                settingsStore.save(updated)
                finish()
            }
        })

        val scrollView = ScrollView(this).apply {
            addView(root)
        }

        setContentView(scrollView)
    }

    private fun soundButton(
        label: String,
        uri: String?,
        selection: SoundSelection
    ): Button {
        return Button(this).apply {
            text = "🔊  " + soundLabel(label, uri)
            textSize = 14f
            setTextColor(Color.rgb(33, 33, 33))
            setAllCaps(false)
            background = roundedBackground(
                Color.WHITE,
                14.dp().toFloat()
            )
            elevation = 3.dp().toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10.dp()
            }

            setOnClickListener {
                pendingSoundSelection = selection
                openSoundPicker(uri)
            }
        }
    }

    private fun openSoundPicker(
        selectedUri: String?
    ) {
        val intent = Intent(
            RingtoneManager.ACTION_RINGTONE_PICKER
        ).apply {
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALARM
            )

            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TITLE,
                "Select alert sound"
            )

            selectedUri
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(it)
                    )
                }
        }

        startActivityForResult(
            intent,
            REQUEST_SOUND
        )
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode != REQUEST_SOUND ||
            resultCode != Activity.RESULT_OK
        ) {
            return
        }

        val uri = data?.getParcelableExtra<Uri>(
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI
        )

        when (pendingSoundSelection) {
            SoundSelection.BATTERY -> {
                selectedBatteryAlertSoundUri =
                    uri?.toString()

                batterySoundButton.text = "🔊  " + soundLabel(
                    "Battery alert sound",
                    selectedBatteryAlertSoundUri
                )
            }

            SoundSelection.POWER_SUPPLY -> {
                selectedPowerSupplyAlertSoundUri =
                    uri?.toString()

                powerSupplySoundButton.text = "🔊  " + soundLabel(
                    "Power supply alert sound",
                    selectedPowerSupplyAlertSoundUri
                )
            }

            SoundSelection.POWER_OFF_LOCKED -> {
                selectedPowerOffLockedSoundUri =
                    uri?.toString()

                powerOffLockedSoundButton.text = "🔊  " + soundLabel(
                    "Power OFF sound — Locked",
                    selectedPowerOffLockedSoundUri
                )
            }

            SoundSelection.POWER_OFF_UNLOCKED -> {
                selectedPowerOffUnlockedSoundUri =
                    uri?.toString()

                powerOffUnlockedSoundButton.text = "🔊  " + soundLabel(
                    "Power OFF sound — Unlocked",
                    selectedPowerOffUnlockedSoundUri
                )

            }

            null -> Unit
        }

        pendingSoundSelection = null
    }

    private fun soundLabel(
        label: String,
        uriString: String?
    ): String {
        if (uriString.isNullOrBlank()) {
            return "$label: System default"
        }

        val ringtone = RingtoneManager.getRingtone(
            this,
            Uri.parse(uriString)
        )

        val title = ringtone?.getTitle(this)

        return if (title.isNullOrBlank()) {
            "$label: Selected"
        } else {
            "$label: $title"
        }
    }

    private fun sectionTitle(title: String): TextView {
        val backgroundColor = when (title) {
            "Battery" -> Color.rgb(232, 245, 233)
            "Power" -> Color.rgb(255, 243, 224)
            "Charging" -> Color.rgb(227, 242, 253)
            else -> Color.rgb(238, 238, 238)
        }

        val textColor = when (title) {
            "Battery" -> Color.rgb(46, 125, 50)
            "Power" -> Color.rgb(239, 108, 0)
            "Charging" -> Color.rgb(21, 101, 192)
            else -> Color.DKGRAY
        }

        return TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
            background = roundedBackground(
                backgroundColor,
                14.dp().toFloat()
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 14.dp()
                bottomMargin = 10.dp()
            }
        }
    }

    private fun checkBox(
        label: String,
        checked: Boolean
    ): CheckBox {
        return CheckBox(this).apply {
            text = label
            textSize = 16f
            isChecked = checked
            setPadding(8.dp(), 4.dp(), 8.dp(), 4.dp())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 2.dp()
            }
        }
    }

    private fun thresholdInput(
        value: Int,
        hint: String
    ): EditText {
        return EditText(this).apply {
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER

            this.hint = hint
            setText(value.toString())
            textSize = 16f
            setPadding(14.dp(), 10.dp(), 14.dp(), 10.dp())
            background = roundedBackground(
                Color.WHITE,
                12.dp().toFloat()
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8.dp()
            }
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun readThreshold(
        input: EditText
    ): Int {
        return input.text
            .toString()
            .toIntOrNull()
            ?.coerceIn(1, 100)
            ?: 50
    }
}
