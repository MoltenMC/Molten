package io.github.moltenmc.molten.common.world.chunk

import java.util.concurrent.CompletableFuture

class ChunkUnloadService(
    private val storage: ChunkStorage,
    private val loadedChunks: LoadedChunkStore,
) {
    fun unload(key: ChunkKey): CompletableFuture<ChunkUnloadResult> {
        val chunk = loadedChunks.get(key)
            ?: return CompletableFuture.completedFuture(ChunkUnloadResult.Missing(key))

        if (chunk.dirtyMarkers.isEmpty()) {
            loadedChunks.remove(key)
            return CompletableFuture.completedFuture(ChunkUnloadResult.Unloaded(key, saved = false))
        }

        return storage.saveChunk(chunk)
            .handle { _, error ->
                if (error != null) {
                    ChunkUnloadResult.Failed(key, chunk, error.unwrapCompletionCause())
                } else {
                    loadedChunks.remove(key)
                    ChunkUnloadResult.Unloaded(key, saved = true)
                }
            }
    }

    private fun Throwable.unwrapCompletionCause(): Throwable =
        cause ?: this
}
