package io.github.moltenmc.molten.server.tick

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ServerTickLoopTest {
    @Test
    fun startsAndStops() {
        val loop = ServerTickLoop(TickPipeline(emptyList()))

        loop.start()
        assertTrue(loop.isRunning)

        loop.stop()
        assertFalse(loop.isRunning)
    }

    @Test
    fun advancesTickCounterAfterEachTick() {
        val observedTicks = mutableListOf<Long>()
        val loop = ServerTickLoop(
            TickPipeline(listOf(TickRecordingTask(observedTicks))),
        )

        loop.start()
        loop.tickOnce().get()
        loop.tickOnce().get()

        assertEquals(listOf(0L, 1L), observedTicks)
        assertEquals(2L, loop.nextTick)
    }

    @Test
    fun notifiesObserverWhenTickCompletes() {
        val observer = RecordingObserver()
        val loop = ServerTickLoop(
            pipeline = TickPipeline(emptyList()),
            observers = listOf(observer),
        )

        loop.start()
        loop.tickOnce().get()

        assertEquals(listOf(0L), observer.startedTicks)
        assertEquals(listOf(0L), observer.completedTicks)
        assertTrue(observer.completedDurations.single() >= 0)
    }

    @Test
    fun rejectsTickWhenStopped() {
        val loop = ServerTickLoop(TickPipeline(emptyList()))

        val error = assertFailsWithExecutionException {
            loop.tickOnce().get()
        }

        assertIs<IllegalStateException>(error.cause)
    }

    @Test
    fun stopsLoopWhenFailurePolicyStops() {
        val failure = IllegalStateException("tick failed")
        val loop = ServerTickLoop(
            pipeline = TickPipeline(listOf(FailingTask(TickPipelineStep.WORLD_UPDATE, failure))),
            failurePolicy = TickFailurePolicy.StopLoop,
        )

        loop.start()
        val result = loop.tickOnce().get()

        val failed = assertIs<ServerTickResult.Failed>(result)
        assertEquals(TickFailureAction.STOP, failed.action)
        assertEquals(TickPipelineStep.WORLD_UPDATE, failed.failure.failedStep)
        assertEquals(failure, failed.failure.cause)
        assertFalse(loop.isRunning)
    }

    @Test
    fun notifiesObserverWhenTickFails() {
        val observer = RecordingObserver()
        val failure = IllegalStateException("tick failed")
        val loop = ServerTickLoop(
            pipeline = TickPipeline(listOf(FailingTask(TickPipelineStep.WORLD_UPDATE, failure))),
            observers = listOf(observer),
        )

        loop.start()
        loop.tickOnce().get()

        assertEquals(listOf(0L), observer.startedTicks)
        assertEquals(listOf(0L), observer.failedTicks)
        assertTrue(observer.failedDurations.single() >= 0)
    }

    @Test
    fun keepsLoopRunningWhenFailurePolicyContinues() {
        val loop = ServerTickLoop(
            pipeline = TickPipeline(
                listOf(
                    FailingTask(TickPipelineStep.WORLD_UPDATE, IllegalStateException("tick failed")),
                ),
            ),
            failurePolicy = TickFailurePolicy.ContinueLoop,
        )

        loop.start()
        val result = loop.tickOnce().get()

        assertIs<ServerTickResult.Failed>(result)
        assertTrue(loop.isRunning)
    }

    @Test
    fun scheduledLoopRunsTicksAtConfiguredRate() {
        val latch = CountDownLatch(2)
        val executor = Executors.newSingleThreadScheduledExecutor()
        val loop = ServerTickLoop(
            TickPipeline(listOf(LatchTask(latch))),
        )
        val handle = loop.startScheduled(TickRate(100), executor)

        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS))
            assertTrue(loop.nextTick >= 2)
        } finally {
            handle.cancel()
            executor.shutdownNow()
        }
    }

    @Test
    fun stopCancelsScheduledLoop() {
        val executor = Executors.newSingleThreadScheduledExecutor()
        val loop = ServerTickLoop(TickPipeline(emptyList()))
        val handle = loop.startScheduled(TickRate(100), executor)

        loop.stop()

        assertTrue(handle.isCancelled)
        assertFalse(loop.isRunning)
        executor.shutdownNow()
    }

    @Test
    fun scheduledLoopDoesNotOverlapTicks() {
        val firstTickStarted = CountDownLatch(1)
        val secondTickStarted = CountDownLatch(1)
        val task = BlockingTask(firstTickStarted, secondTickStarted)
        val observer = RecordingObserver()
        val executor = Executors.newSingleThreadScheduledExecutor()
        val loop = ServerTickLoop(
            pipeline = TickPipeline(listOf(task)),
            observers = listOf(observer),
        )
        val handle = loop.startScheduled(TickRate(1_000), executor)

        try {
            assertTrue(firstTickStarted.await(1, TimeUnit.SECONDS))
            Thread.sleep(50)
            assertEquals(1, task.startedTicks.get())
            assertTrue(observer.skippedReasons.contains(TickSkipReason.PREVIOUS_TICK_RUNNING))

            task.completeCurrentTick()

            assertTrue(secondTickStarted.await(1, TimeUnit.SECONDS))
            assertTrue(task.startedTicks.get() >= 2)
        } finally {
            handle.cancel()
            executor.shutdownNow()
        }
    }

    @Test
    fun observerFailureDoesNotFailTick() {
        val loop = ServerTickLoop(
            pipeline = TickPipeline(emptyList()),
            observers = listOf(ThrowingObserver()),
        )

        loop.start()
        val result = loop.tickOnce().get()

        assertIs<ServerTickResult.Completed>(result)
        assertTrue(loop.isRunning)
    }

    private fun assertFailsWithExecutionException(block: () -> Unit): ExecutionException =
        try {
            block()
            throw AssertionError("Expected ExecutionException.")
        } catch (error: ExecutionException) {
            error
        }

    private class TickRecordingTask(
        private val observedTicks: MutableList<Long>,
    ) : TickTask {
        override val step: TickPipelineStep = TickPipelineStep.WORLD_UPDATE

        override fun execute(currentTick: Long): CompletableFuture<Unit> {
            observedTicks += currentTick
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

    private class LatchTask(
        private val latch: CountDownLatch,
    ) : TickTask {
        override val step: TickPipelineStep = TickPipelineStep.WORLD_UPDATE

        override fun execute(currentTick: Long): CompletableFuture<Unit> {
            latch.countDown()
            return CompletableFuture.completedFuture(Unit)
        }
    }

    private class BlockingTask(
        private val firstTickStarted: CountDownLatch,
        private val secondTickStarted: CountDownLatch,
    ) : TickTask {
        override val step: TickPipelineStep = TickPipelineStep.WORLD_UPDATE

        val startedTicks = AtomicInteger(0)
        private val currentTick = CompletableFuture<Unit>()

        override fun execute(currentTick: Long): CompletableFuture<Unit> {
            val started = startedTicks.incrementAndGet()
            if (started == 1) {
                firstTickStarted.countDown()
            }
            if (started == 2) {
                secondTickStarted.countDown()
            }
            return this.currentTick
        }

        fun completeCurrentTick() {
            currentTick.complete(Unit)
        }
    }

    private class RecordingObserver : TickObserver {
        val startedTicks = mutableListOf<Long>()
        val completedTicks = mutableListOf<Long>()
        val completedDurations = mutableListOf<Long>()
        val failedTicks = mutableListOf<Long>()
        val failedDurations = mutableListOf<Long>()
        val skippedReasons = mutableListOf<TickSkipReason>()

        override fun onTickStarted(currentTick: Long) {
            startedTicks += currentTick
        }

        override fun onTickCompleted(result: ServerTickResult.Completed, durationNanos: Long) {
            completedTicks += result.currentTick
            completedDurations += durationNanos
        }

        override fun onTickFailed(result: ServerTickResult.Failed, durationNanos: Long) {
            failedTicks += result.currentTick
            failedDurations += durationNanos
        }

        override fun onScheduledTickSkipped(reason: TickSkipReason) {
            skippedReasons += reason
        }
    }

    private class ThrowingObserver : TickObserver {
        override fun onTickStarted(currentTick: Long) {
            throw IllegalStateException("observer failed")
        }
    }
}
