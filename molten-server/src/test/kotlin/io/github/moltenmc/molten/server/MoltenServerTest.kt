package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.server.tick.InMemoryTickMetricsObserver
import io.github.moltenmc.molten.server.tick.ServerTickLoop
import io.github.moltenmc.molten.server.tick.TickRate
import io.github.moltenmc.molten.server.tick.TickTask
import io.github.moltenmc.molten.server.tick.TickPipeline
import io.github.moltenmc.molten.server.tick.TickPipelineStep
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoltenServerTest {
    @Test
    fun startsTickLoopWhenServerStarts() {
        val tickLoop = ServerTickLoop(TickPipeline(emptyList()))
        val server = MoltenServer(
            configuration = ServerConfiguration.defaults(),
            tickLoop = tickLoop,
            tickMetrics = InMemoryTickMetricsObserver(),
        )

        server.start()

        assertEquals(LifecycleState.RUNNING, server.state)
        assertTrue(tickLoop.isRunning)
    }

    @Test
    fun stopsTickLoopWhenServerStops() {
        val tickLoop = ServerTickLoop(TickPipeline(emptyList()))
        val server = MoltenServer(
            configuration = ServerConfiguration.defaults(),
            tickLoop = tickLoop,
            tickMetrics = InMemoryTickMetricsObserver(),
        )

        server.start()
        server.stop()

        assertEquals(LifecycleState.STOPPED, server.state)
        assertFalse(tickLoop.isRunning)
    }

    @Test
    fun exposesTickMetricsSnapshot() {
        val tickMetrics = InMemoryTickMetricsObserver()
        val server = MoltenServer(
            configuration = ServerConfiguration.defaults(),
            tickLoop = ServerTickLoop(
                pipeline = TickPipeline(emptyList()),
                observers = listOf(tickMetrics),
            ),
            tickMetrics = tickMetrics,
        )

        server.start()
        server.tickOnce().get()
        server.tickOnce().get()

        val metrics = server.tickMetrics()
        assertEquals(2, metrics.startedTicks)
        assertEquals(2, metrics.completedTicks)
        assertEquals(1, metrics.lastCompletedTick)
    }

    @Test
    fun createdServerStartsScheduledTicks() {
        val latch = CountDownLatch(1)
        val server = MoltenServer.create(
            configuration = ServerConfiguration.defaults().copy(tickRate = TickRate(100)),
            tickPipeline = TickPipeline(listOf(LatchTask(latch))),
        )

        try {
            server.start()

            assertTrue(latch.await(1, TimeUnit.SECONDS))
            assertTrue(server.tickMetrics().startedTicks >= 1)
        } finally {
            server.stop()
        }
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
}
