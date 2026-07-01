package io.github.moltenmc.molten.api.event.block

import io.github.moltenmc.molten.api.entity.player.Player
import io.github.moltenmc.molten.api.event.CancellableEvent
import io.github.moltenmc.molten.api.event.EventExecutionType
import io.github.moltenmc.molten.common.world.BlockPos

data class BlockBreakEvent(
    val player: Player,
    val position: BlockPos,
    override var cancelled: Boolean = false,
) : CancellableEvent {
    override val executionType: EventExecutionType = EventExecutionType.SYNC_REGION
}
