package io.github.moltenmc.molten.server.tick

data class TickFailure(
    val currentTick: Long,
    val failedStep: TickPipelineStep,
    val executedSteps: List<TickPipelineStep>,
    val cause: Throwable,
)
