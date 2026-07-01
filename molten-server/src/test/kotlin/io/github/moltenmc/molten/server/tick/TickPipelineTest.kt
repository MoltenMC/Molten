package io.github.moltenmc.molten.server.tick

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TickPipelineTest {
    @Test
    fun executesTasksInPipelineOrder() {
        val observed = mutableListOf<TickPipelineStep>()
        val pipeline = TickPipeline(
            listOf(
                RecordingTask(TickPipelineStep.CLEANUP, observed),
                RecordingTask(TickPipelineStep.NETWORK_INGRESS, observed),
                RecordingTask(TickPipelineStep.WORLD_UPDATE, observed),
            ),
        )

        val result = pipeline.execute(currentTick = 42).get()

        assertEquals(
            listOf(
                TickPipelineStep.NETWORK_INGRESS,
                TickPipelineStep.WORLD_UPDATE,
                TickPipelineStep.CLEANUP,
            ),
            observed,
        )
        assertEquals(observed, result.executedSteps)
    }

    @Test
    fun passesCurrentTickToTasks() {
        val ticks = mutableListOf<Long>()
        val pipeline = TickPipeline(
            listOf(
                TickRecordingTask(TickPipelineStep.WORLD_UPDATE, ticks),
                TickRecordingTask(TickPipelineStep.CLEANUP, ticks),
            ),
        )

        pipeline.execute(currentTick = 99).get()

        assertEquals(listOf(99L, 99L), ticks)
    }

    @Test
    fun reportsFailedStepAndAlreadyExecutedSteps() {
        val failure = IllegalStateException("task failed")
        val observed = mutableListOf<TickPipelineStep>()
        val pipeline = TickPipeline(
            listOf(
                RecordingTask(TickPipelineStep.NETWORK_INGRESS, observed),
                FailingTask(TickPipelineStep.WORLD_UPDATE, failure),
                RecordingTask(TickPipelineStep.CLEANUP, observed),
            ),
        )

        val error = assertFailsWithExecutionException {
            pipeline.execute(currentTick = 7).get()
        }

        val pipelineError = assertIs<TickPipelineException>(error.cause)
        assertEquals(7L, pipelineError.failure.currentTick)
        assertEquals(TickPipelineStep.WORLD_UPDATE, pipelineError.failure.failedStep)
        assertEquals(listOf(TickPipelineStep.NETWORK_INGRESS), pipelineError.failure.executedSteps)
        assertEquals(failure, pipelineError.failure.cause)
    }

    private fun assertFailsWithExecutionException(block: () -> Unit): ExecutionException =
        try {
            block()
            throw AssertionError("Expected ExecutionException.")
        } catch (error: ExecutionException) {
            error
        }

    private class RecordingTask(
        override val step: TickPipelineStep,
        private val observed: MutableList<TickPipelineStep>,
    ) : TickTask {
        override fun execute(currentTick: Long): CompletableFuture<Unit> {
            observed += step
            return CompletableFuture.completedFuture(Unit)
        }
    }

    private class TickRecordingTask(
        override val step: TickPipelineStep,
        private val ticks: MutableList<Long>,
    ) : TickTask {
        override fun execute(currentTick: Long): CompletableFuture<Unit> {
            ticks += currentTick
            return CompletableFuture.completedFuture(Unit)
        }
    }

    private class FailingTask(
        override val step: TickPipelineStep,
        private val failure: Throwable,
    ) : TickTask {
        override fun execute(currentTick: Long): CompletableFuture<Unit> =
            CompletableFuture.failedFuture(failure)
    }
}
