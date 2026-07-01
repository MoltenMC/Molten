package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import java.util.concurrent.CompletableFuture

class WorldChunkService(
    private val loadingCoordinator: ChunkLoadingCoordinator,
    private val lifecycleService: ChunkLifecycleService,
    private val loadedChunks: LoadedChunkStore,
    private val tickets: ChunkTicketRegistry,
) {
    fun loadChunk(
        worldId: WorldId,
        dimensionId: DimensionId,
        position: ChunkPos,
        tickets: Set<ChunkTicket>,
    ): CompletableFuture<ChunkLoadResult> =
        loadingCoordinator.load(
            ChunkLoadRequest(
                worldId = worldId,
                dimensionId = dimensionId,
                position = position,
                tickets = tickets,
            ),
        )

    fun loadedChunk(key: ChunkKey): Chunk? =
        loadedChunks.get(key)

    fun ticketsFor(key: ChunkKey): Set<ChunkTicket> =
        tickets.ticketsFor(key)

    fun cleanupExpiredTickets(currentTick: Long): CompletableFuture<ChunkLifecycleResult> =
        lifecycleService.cleanupExpiredTickets(currentTick)

    companion object {
        fun create(storage: ChunkStorage): WorldChunkService {
            val loadedChunks = LoadedChunkStore()
            val tickets = ChunkTicketRegistry()
            val loadService = ChunkLoadService(storage)
            val unloadService = ChunkUnloadService(storage, loadedChunks)
            return WorldChunkService(
                loadingCoordinator = ChunkLoadingCoordinator(loadService, loadedChunks, tickets),
                lifecycleService = ChunkLifecycleService(tickets, unloadService),
                loadedChunks = loadedChunks,
                tickets = tickets,
            )
        }
    }
}
