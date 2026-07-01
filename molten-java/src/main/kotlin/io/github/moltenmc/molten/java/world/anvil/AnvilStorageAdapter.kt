package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.Chunk
import java.util.concurrent.CompletableFuture

interface AnvilStorageAdapter {
    fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?>

    fun saveChunk(chunk: Chunk): CompletableFuture<Void>
}
