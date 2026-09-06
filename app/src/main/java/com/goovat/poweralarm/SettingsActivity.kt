package com.goovat.poweralarm

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
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
            setPadding(40, 40, 40, 40)
        }

        root.addView(TextView(this).apply {
            text = "Power Alarm Settings"
            textSize = 26f
        })

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
            text = "Save Settings"

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

        setContentView(root)
    }

    private fun soundButton(
        label: String,
        uri: String?,
        selection: SoundSelection
    ): Button {
        return Button(this).apply {
            text = soundLabel(label, uri)

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

                batterySoundButton.text = soundLabel(
                    "Battery alert sound",
                    selectedBatteryAlertSoundUri
                )
            }

            SoundSelection.POWER_SUPPLY -> {
                selectedPowerSupplyAlertSoundUri =
                    uri?.toString()

                powerSupplySoundButton.text = soundLabel(
                    "Power supply alert sound",
                    selectedPowerSupplyAlertSoundUri
                )
            }

            SoundSelection.POWER_OFF_LOCKED -> {
                selectedPowerOffLockedSoundUri =
                    uri?.toString()

                powerOffLockedSoundButton.text = soundLabel(
                    "Power OFF sound — Locked",
                    selectedPowerOffLockedSoundUri
                )
            }

            SoundSelection.POWER_OFF_UNLOCKED -> {
                selectedPowerOffUnlockedSoundUri =
                    uri?.toString()

                powerOffUnlockedSoundButton.text = soundLabel(
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
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER

            this.hint = hint
            setText(value.toString())
        }
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
