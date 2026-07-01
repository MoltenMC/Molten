package io.github.moltenmc.molten.server.tick

data class TickPipelineResult(
    val executedSteps: List<TickPipelineStep>,
)
