package io.github.moltenmc.molten.api.event.server

import io.github.moltenmc.molten.api.event.Event
import io.github.moltenmc.molten.api.event.EventExecutionType

data class MetricsEvent(val values: Map<String, Double>) : Event {
    override val executionType: EventExecutionType = EventExecutionType.ASYNC_OBSERVATIONAL
}
