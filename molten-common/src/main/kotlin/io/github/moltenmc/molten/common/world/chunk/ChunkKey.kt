package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId

data class ChunkKey(
    val worldId: WorldId,
    val dimensionId: DimensionId,
    val position: ChunkPos,
) {
    companion object {
        fun from(request: ChunkLoadRequest): ChunkKey =
            ChunkKey(
                worldId = request.worldId,
                dimensionId = request.dimensionId,
                position = request.position,
            )
    }
}
