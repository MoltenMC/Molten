package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.world.chunk.ChunkStorage

interface AnvilStorageAdapter : ChunkStorage, AutoCloseable {
    override fun close()
}
