package io.github.moltenmc.molten.api.event

data class EventSubscription<T : Event>(
    val eventType: Class<T>,
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    val listener: (T) -> Unit,
)
