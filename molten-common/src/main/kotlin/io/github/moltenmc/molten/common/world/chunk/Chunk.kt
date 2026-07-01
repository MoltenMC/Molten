package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.section.ChunkSection

data class Chunk(
    val position: ChunkPos,
    val sections: List<ChunkSection>,
    val dirtyMarkers: Set<DirtyMarker> = emptySet(),
)
