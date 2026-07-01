package io.github.moltenmc.molten.api.event

interface CancellableEvent : Event {
    var cancelled: Boolean
}
