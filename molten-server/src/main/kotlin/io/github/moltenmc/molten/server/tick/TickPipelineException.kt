package io.github.moltenmc.molten.server.tick

class TickPipelineException(
    val failure: TickFailure,
) : RuntimeException(
    "Tick ${failure.currentTick} failed at ${failure.failedStep}.",
    failure.cause,
)
