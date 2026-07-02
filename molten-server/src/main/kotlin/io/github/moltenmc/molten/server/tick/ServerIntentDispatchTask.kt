package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.common.network.intent.ServerIntentSink
import io.github.moltenmc.molten.server.network.ServerIntentInbox
import java.util.concurrent.CompletableFuture

class ServerIntentDispatchTask(
    private val inbox: ServerIntentInbox,
    private val intentSink: ServerIntentSink = ServerIntentSink.Noop,
) : TickTask {
    override val step: TickPipelineStep = TickPipelineStep.INTENT_ROUTING

    override fun execute(currentTick: Long): CompletableFuture<Unit> {
        dispatch()
        return CompletableFuture.completedFuture(Unit)
    }

    fun dispatch(): Int {
        val intents = inbox.drain()
        intentSink.acceptAll(intents)
        return intents.size
    }
}
