package io.github.moltenmc.molten.server.tick

import java.util.concurrent.CompletableFuture

class TickPipeline(
    tasks: Iterable<TickTask>,
) {
    private val orderedTasks = tasks.sortedBy { task -> task.step.order }

    fun execute(currentTick: Long): CompletableFuture<TickPipelineResult> {
        val executedSteps = mutableListOf<TickPipelineStep>()
        var chain = CompletableFuture.completedFuture(Unit)

        orderedTasks.forEach { task ->
            chain = chain.thenCompose {
                task.execute(currentTick)
                    .thenApply {
                        executedSteps += task.step
                        Unit
                    }
                    .exceptionallyCompose { error ->
                        CompletableFuture.failedFuture(
                            TickPipelineException(
                                TickFailure(
                                    currentTick = currentTick,
                                    failedStep = task.step,
                                    executedSteps = executedSteps.toList(),
                                    cause = error.unwrapCompletionCause(),
                                ),
                            ),
                        )
                    }
            }
        }

        return chain.thenApply {
            TickPipelineResult(executedSteps = executedSteps.toList())
        }
    }

    private fun Throwable.unwrapCompletionCause(): Throwable =
        cause ?: this
}
