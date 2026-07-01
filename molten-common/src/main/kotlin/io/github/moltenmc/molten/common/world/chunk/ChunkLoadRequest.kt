package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId

data class ChunkLoadRequest(
    val worldId: WorldId,
    val dimensionId: DimensionId,
    val position: ChunkPos,
    val tickets: Set<ChunkTicket>,
) {
    init {
        require(tickets.isNotEmpty()) { "Chunk load requests require at least one ticket." }
    }

    val highestPriorityTicket: ChunkTicket
        get() = tickets.minWith(ChunkTicket.priorityOrder)
}
