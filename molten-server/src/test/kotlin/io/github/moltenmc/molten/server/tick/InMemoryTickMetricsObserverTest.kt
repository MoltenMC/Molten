package io.github.moltenmc.molten.server.tick

import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryTickMetricsObserverTest {
    @Test
    fun recordsCompletedTickMetrics() {
        val observer = InMemoryTickMetricsObserver()
        val loop = ServerTickLoop(
            pipeline = TickPipeline(emptyList()),
            observers = listOf(observer),
        )

        loop.start()
        loop.tickOnce().get()
        loop.tickOnce().get()

        val snapshot = observer.snapshot()
        assertEquals(2, snapshot.startedTicks)
        assertEquals(2, snapshot.completedTicks)
        assertEquals(0, snapshot.failedTicks)
        assertEquals(1, snapshot.lastCompletedTick)
        assertTrue(snapshot.totalDurationNanos >= 0)
        assertTrue(snapshot.maxDurationNanos >= 0)
    }

    @Test
    fun recordsFailedTickMetrics() {
        val failure = IllegalStateException("tick failed")
        val observer = InMemoryTickMetricsObserver()
        val loop = ServerTickLoop(
            pipeline = TickPipeline(listOf(FailingTask(failure))),
            failurePolicy = TickFailurePolicy.ContinueLoop,
            observers = listOf(observer),
        )

        loop.start()
        loop.tickOnce().get()

        val snapshot = observer.snapshot()
        assertEquals(1, snapshot.startedTicks)
        assertEquals(0, snapshot.completedTicks)
        assertEquals(1, snapshot.failedTicks)
        assertEquals(0, snapshot.lastFailedTick)
        assertEquals(failure, snapshot.lastFailure?.cause)
    }

    @Test
    fun recordsSkippedTicksByReason() {
        val observer = InMemoryTickMetricsObserver()

        observer.onScheduledTickSkipped(TickSkipReason.NOT_RUNNING)
        observer.onScheduledTickSkipped(TickSkipReason.PREVIOUS_TICK_RUNNING)
        observer.onScheduledTickSkipped(TickSkipReason.PREVIOUS_TICK_RUNNING)

        val snapshot = observer.snapshot()
        assertEquals(3, snapshot.skippedTicks)
        assertEquals(1, snapshot.skippedByReason.getValue(TickSkipReason.NOT_RUNNING))
        assertEquals(2, snapshot.skippedByReason.getValue(TickSkipReason.PREVIOUS_TICK_RUNNING))
    }

    @Test
    fun calculatesAverageDuration() {
        val observer = InMemoryTickMetricsObserver()

        observer.onTickCompleted(
            ServerTickResult.Completed(0, TickPipelineResult(emptyList())),
            durationNanos = 10,
        )
        observer.onTickFailed(
            ServerTickResult.Failed(
                currentTick = 1,
                failure = TickFailure(
                    currentTick = 1,
                    failedStep = TickPipelineStep.WORLD_UPDATE,
                    executedSteps = emptyList(),
                    cause = IllegalStateException("failed"),
                ),
                action = TickFailureAction.CONTINUE,
            ),
            durationNanos = 30,
        )

        assertEquals(20, observer.snapshot().averageDurationNanos)
    }

    private class FailingTask(
        private val failure: Throwable,
    ) : TickTask {
        override val step: TickPipelineStep = TickPipelineStep.WORLD_UPDATE

        override fun execute(currentTick: Long): CompletableFuture<Unit> =
            CompletableFuture.failedFuture(failure)
    }
}
