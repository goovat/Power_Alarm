package com.goovat.poweralarm

class PowerEventHandler(
    private val eventEvaluator: AlarmEventEvaluator
) {

    fun evaluate(
        previous: PowerSnapshot?,
        current: PowerSnapshot,
        settings: AlarmSettings
    ): List<AlarmEvent> {
        if (previous == null) {
            return emptyList()
        }

        return eventEvaluator.evaluatePowerEvents(
            previous = previous,
            current = current,
            settings = settings
        )
    }
}
