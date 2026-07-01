package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.world.ChunkPos
import java.util.concurrent.CompletableFuture

interface ChunkStorage {
    fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?>

    fun saveChunk(chunk: Chunk): CompletableFuture<Void>
}
