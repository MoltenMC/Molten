package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.server.network.intent.NoopRegionIntentProcessor
import io.github.moltenmc.molten.server.network.intent.RegionIntentInbox
import io.github.moltenmc.molten.server.network.intent.RegionIntentProcessor
import java.util.concurrent.CompletableFuture

class RegionIntentSimulationTask(
    private val inbox: RegionIntentInbox,
    private val processor: RegionIntentProcessor = NoopRegionIntentProcessor(),
) : TickTask {
    override val step: TickPipelineStep = TickPipelineStep.REGION_SIMULATION

    override fun execute(currentTick: Long): CompletableFuture<Unit> {
        return try {
            val processedCount = drainAndProcess()
            if (processedCount > 0) {
                // TODO: Publish to metrics observer when available
                // For now, this count is available for debugging and future observability
            }
            CompletableFuture.completedFuture(Unit)
        } catch (error: Throwable) {
            CompletableFuture.failedFuture(error)
        }
    }

    fun drainAndProcess(): Int {
        val batches = inbox.drainAll()
        batches.forEach(processor::process)
        return batches.sumOf { batch -> batch.intents.size }
    }
}
