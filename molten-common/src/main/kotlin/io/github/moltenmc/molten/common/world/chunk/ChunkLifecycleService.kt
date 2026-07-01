package io.github.moltenmc.molten.common.world.chunk

import java.util.concurrent.CompletableFuture

class ChunkLifecycleService(
    private val tickets: ChunkTicketRegistry,
    private val unloadService: ChunkUnloadService,
) {
    fun cleanupExpiredTickets(currentTick: Long): CompletableFuture<ChunkLifecycleResult> {
        val unloadCandidates = tickets.cleanupExpired(currentTick)
        if (unloadCandidates.isEmpty()) {
            return CompletableFuture.completedFuture(
                ChunkLifecycleResult(
                    unloadCandidates = emptySet(),
                    unloadResults = emptyList(),
                ),
            )
        }

        val unloads = unloadCandidates.map { key -> unloadService.unload(key) }
        return CompletableFuture.allOf(*unloads.toTypedArray())
            .thenApply {
                ChunkLifecycleResult(
                    unloadCandidates = unloadCandidates,
                    unloadResults = unloads.map { unload -> unload.join() },
                )
            }
    }
}
