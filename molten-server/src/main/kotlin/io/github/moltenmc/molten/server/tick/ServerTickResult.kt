package io.github.moltenmc.molten.server.tick

sealed interface ServerTickResult {
    val currentTick: Long

    data class Completed(
        override val currentTick: Long,
        val pipelineResult: TickPipelineResult,
    ) : ServerTickResult

    data class Failed(
        override val currentTick: Long,
        val failure: TickFailure,
        val action: TickFailureAction,
    ) : ServerTickResult
}
