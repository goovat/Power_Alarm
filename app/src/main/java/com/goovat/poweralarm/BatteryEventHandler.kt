package com.goovat.poweralarm

class BatteryEventHandler(
    private val eventEvaluator: AlarmEventEvaluator
) {

    fun evaluate(
        previous: BatterySnapshot?,
        current: BatterySnapshot,
        settings: AlarmSettings
    ): List<AlarmEvent> {
        if (previous == null) {
            return emptyList()
        }

        return eventEvaluator.evaluateBatteryEvents(
            previous = previous,
            current = current,
            settings = settings
        )
    }
}
