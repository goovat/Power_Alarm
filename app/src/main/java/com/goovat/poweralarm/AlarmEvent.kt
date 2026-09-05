package com.goovat.poweralarm

sealed class AlarmEvent {

    data object PowerOff : AlarmEvent()

    data object PowerRestored : AlarmEvent()

    data object ChargingStarted : AlarmEvent()

    data object ChargingStopped : AlarmEvent()

    data class LowBattery(
        val percentage: Int
    ) : AlarmEvent()

    data class CriticalBattery(
        val percentage: Int
    ) : AlarmEvent()

    data class FullBattery(
        val percentage: Int
    ) : AlarmEvent()
}
