package io.github.moltenmc.molten.common.world.chunk

import java.util.concurrent.CompletableFuture

class ChunkLoadingCoordinator(
    private val loadService: ChunkLoadService,
    private val loadedChunks: LoadedChunkStore,
    private val tickets: ChunkTicketRegistry,
) {
    fun load(request: ChunkLoadRequest): CompletableFuture<ChunkLoadResult> {
        val key = ChunkKey.from(request)
        tickets.addTickets(key, request.tickets)

        val loaded = loadedChunks.get(key)
        if (loaded != null) {
            return CompletableFuture.completedFuture(ChunkLoadResult.Loaded(request, loaded))
        }

        return loadService.load(request)
            .thenApply { result ->
                if (result is ChunkLoadResult.Loaded) {
                    loadedChunks.put(key, result.chunk)
                }
                result
            }
    }
}
