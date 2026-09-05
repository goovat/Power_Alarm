package com.goovat.poweralarm

class AlarmEventEvaluator {

    fun evaluatePowerEvents(
        previous: PowerSnapshot,
        current: PowerSnapshot,
        settings: AlarmSettings
    ): List<AlarmEvent> {
        val events = mutableListOf<AlarmEvent>()

        if (
            previous.isExternalPowerConnected &&
            !current.isExternalPowerConnected &&
            settings.powerOffEnabled
        ) {
            events += AlarmEvent.PowerOff
        }

        if (
            !previous.isExternalPowerConnected &&
            current.isExternalPowerConnected &&
            settings.powerRestoredEnabled
        ) {
            events += AlarmEvent.PowerRestored
        }

        if (
            !previous.isCharging &&
            current.isCharging &&
            settings.chargingStartedEnabled
        ) {
            events += AlarmEvent.ChargingStarted
        }

        if (
            previous.isCharging &&
            !current.isCharging &&
            settings.chargingStoppedEnabled
        ) {
            events += AlarmEvent.ChargingStopped
        }

        return events
    }

    fun evaluateBatteryEvents(
        previous: BatterySnapshot,
        current: BatterySnapshot,
        settings: AlarmSettings
    ): List<AlarmEvent> {
        val events = mutableListOf<AlarmEvent>()

        if (
            settings.criticalBatteryEnabled &&
            crossedDownward(
                previous.percentage,
                current.percentage,
                settings.criticalBatteryThreshold
            )
        ) {
            events += AlarmEvent.CriticalBattery(current.percentage)
        } else if (
            settings.lowBatteryEnabled &&
            crossedDownward(
                previous.percentage,
                current.percentage,
                settings.lowBatteryThreshold
            )
        ) {
            events += AlarmEvent.LowBattery(current.percentage)
        }

        if (
            settings.fullBatteryEnabled &&
            crossedUpward(
                previous.percentage,
                current.percentage,
                settings.fullBatteryThreshold
            )
        ) {
            events += AlarmEvent.FullBattery(current.percentage)
        }

        return events
    }

    private fun crossedDownward(
        previous: Int,
        current: Int,
        threshold: Int
    ): Boolean {
        return previous > threshold && current <= threshold
    }

    private fun crossedUpward(
        previous: Int,
        current: Int,
        threshold: Int
    ): Boolean {
        return previous < threshold && current >= threshold
    }
}
