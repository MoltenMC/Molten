package io.github.moltenmc.molten.api.event.player

import io.github.moltenmc.molten.api.entity.player.Player
import io.github.moltenmc.molten.api.event.Event
import io.github.moltenmc.molten.api.event.EventExecutionType

data class PlayerJoinEvent(val player: Player) : Event {
    override val executionType: EventExecutionType = EventExecutionType.SYNC_REGION
}
