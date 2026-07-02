package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.server.network.intent.RegionIntentInbox
import io.github.moltenmc.molten.server.network.intent.RegionIntentProcessor
import java.util.concurrent.CompletableFuture

class RegionIntentSimulationTask(
    private val inbox: RegionIntentInbox,
    private val processor: RegionIntentProcessor = RegionIntentProcessor.Noop,
) : TickTask {
    override val step: TickPipelineStep = TickPipelineStep.REGION_SIMULATION

    override fun execute(currentTick: Long): CompletableFuture<Unit> {
        drainAndProcess()
        return CompletableFuture.completedFuture(Unit)
    }

    fun drainAndProcess(): Int {
        val batches = inbox.drainAll()
        batches.forEach(processor::process)
        return batches.sumOf { batch -> batch.intents.size }
    }
}
