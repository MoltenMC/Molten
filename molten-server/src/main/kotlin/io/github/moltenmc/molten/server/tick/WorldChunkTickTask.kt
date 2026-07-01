package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.common.world.chunk.WorldChunkService
import java.util.concurrent.CompletableFuture

class WorldChunkTickTask(
    private val chunks: WorldChunkService,
) : TickTask {
    override val step: TickPipelineStep = TickPipelineStep.WORLD_UPDATE

    override fun execute(currentTick: Long): CompletableFuture<Unit> =
        chunks.cleanupExpiredTickets(currentTick)
            .thenApply {}
}
