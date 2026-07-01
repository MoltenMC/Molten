package io.github.moltenmc.molten.common.spi

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.WorldStorageKind
import io.github.moltenmc.molten.common.world.chunk.Chunk
import java.util.concurrent.CompletableFuture

interface WorldStorageProvider {
    val storageKind: WorldStorageKind

    fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?>

    fun saveChunk(chunk: Chunk): CompletableFuture<Void>
}
