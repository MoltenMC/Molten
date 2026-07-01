package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.Chunk
import java.util.concurrent.CompletableFuture

interface LevelDbStorageAdapter : AutoCloseable {
    fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?>

    fun saveChunk(chunk: Chunk): CompletableFuture<Void>

    override fun close()
}
