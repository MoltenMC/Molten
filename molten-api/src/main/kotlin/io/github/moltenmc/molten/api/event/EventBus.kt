package io.github.moltenmc.molten.api.event

interface EventBus {
    fun <T : Event> subscribe(eventType: Class<T>, listener: (T) -> Unit)

    fun <T : Event> subscribe(subscription: EventSubscription<T>)

    fun publish(event: Event)
}
