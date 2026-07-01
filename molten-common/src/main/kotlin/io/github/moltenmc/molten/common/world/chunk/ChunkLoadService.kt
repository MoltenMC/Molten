package io.github.moltenmc.molten.common.world.chunk

import java.util.concurrent.CompletableFuture

class ChunkLoadService(
    private val storage: ChunkStorage,
) {
    fun load(request: ChunkLoadRequest): CompletableFuture<ChunkLoadResult> =
        storage.loadChunk(request.position)
            .handle { chunk, error ->
                when {
                    error != null -> ChunkLoadResult.Failed(request, error.unwrapCompletionCause())
                    chunk != null -> ChunkLoadResult.Loaded(request, chunk)
                    else -> ChunkLoadResult.Missing(request)
                }
            }

    private fun Throwable.unwrapCompletionCause(): Throwable =
        cause ?: this
}
