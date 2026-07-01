package io.github.moltenmc.molten.server.tick

import java.util.concurrent.CompletableFuture

interface TickTask {
    val step: TickPipelineStep

    fun execute(currentTick: Long): CompletableFuture<Unit>
}
