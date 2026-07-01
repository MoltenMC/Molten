package io.github.moltenmc.molten.server.world

import io.github.moltenmc.molten.common.world.WorldStorageKind
import io.github.moltenmc.molten.common.world.chunk.ChunkStorage
import io.github.moltenmc.molten.common.world.chunk.WorldChunkService

class WorldStorageRuntime(
    val storageKind: WorldStorageKind,
    val storage: ChunkStorage,
    val chunks: WorldChunkService,
    private val closeStorage: () -> Unit,
) : AutoCloseable {
    override fun close() {
        closeStorage()
    }
}
