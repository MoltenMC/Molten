package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.world.ChunkPos

data class AnvilChunkCoordinates(
    val chunk: ChunkPos,
    val regionX: Int,
    val regionZ: Int,
    val localX: Int,
    val localZ: Int,
) {
    companion object {
        fun fromChunkPos(position: ChunkPos): AnvilChunkCoordinates =
            AnvilChunkCoordinates(
                chunk = position,
                regionX = position.x shr REGION_SHIFT,
                regionZ = position.z shr REGION_SHIFT,
                localX = position.x and LOCAL_MASK,
                localZ = position.z and LOCAL_MASK,
            )

        private const val REGION_SHIFT = 5
        private const val LOCAL_MASK = 31
    }
}
