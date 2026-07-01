package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.anvil.Anvil
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.Chunk
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

class DefaultAnvilStorageAdapter(
    regionDirectory: Path,
    private val mapper: AnvilChunkMapper = DefaultAnvilChunkMapper(),
    private val ioExecutor: Executor = ForkJoinPool.commonPool(),
) : AnvilStorageAdapter {
    private val anvil = Anvil(regionDirectory)

    override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
        CompletableFuture.supplyAsync(
            {
                if (!anvil.hasChunk(position.x, position.z)) {
                    return@supplyAsync null
                }

                val rawChunkData = anvil.readChunk(position.x, position.z) ?: return@supplyAsync null
                mapper.toChunk(position, rawChunkData)
            },
            ioExecutor,
        )

    override fun saveChunk(chunk: Chunk): CompletableFuture<Void> =
        CompletableFuture.runAsync(
            {
                val rawChunkData = mapper.toRawChunkData(chunk)
                anvil.writeChunk(chunk.position.x, chunk.position.z, rawChunkData)
            },
            ioExecutor,
        )

    override fun close() {
        anvil.close()
    }
}
