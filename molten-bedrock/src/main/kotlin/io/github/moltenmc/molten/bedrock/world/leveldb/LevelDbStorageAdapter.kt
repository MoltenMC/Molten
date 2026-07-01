package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.chunk.ChunkStorage

interface LevelDbStorageAdapter : ChunkStorage, AutoCloseable {
    override fun close()
}
