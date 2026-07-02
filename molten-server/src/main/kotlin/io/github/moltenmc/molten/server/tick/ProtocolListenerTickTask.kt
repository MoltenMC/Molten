package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.server.network.ProtocolListener
import java.util.concurrent.CompletableFuture

class ProtocolListenerTickTask(
    private val listeners: Iterable<ProtocolListener>,
    override val step: TickPipelineStep = TickPipelineStep.NETWORK_EGRESS,
    private val tickListener: (ProtocolListener) -> Int = ProtocolListener::tickEgress,
) : TickTask {
    override fun execute(currentTick: Long): CompletableFuture<Unit> {
        listeners.forEach { listener -> tickListener(listener) }
        return CompletableFuture.completedFuture(Unit)
    }
}
