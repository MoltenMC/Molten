package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.server.network.ProtocolListener
import java.util.concurrent.CompletableFuture

class ProtocolListenerTickTask(
    private val listeners: Iterable<ProtocolListener>,
) : TickTask {
    override val step: TickPipelineStep = TickPipelineStep.NETWORK_EGRESS

    override fun execute(currentTick: Long): CompletableFuture<Unit> {
        listeners.forEach { listener -> listener.tick() }
        return CompletableFuture.completedFuture(Unit)
    }
}
