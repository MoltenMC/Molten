package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.server.tick.InMemoryTickMetricsObserver
import io.github.moltenmc.molten.server.tick.ServerTickLoop
import io.github.moltenmc.molten.server.tick.ServerTickResult
import io.github.moltenmc.molten.server.tick.TickPipeline
import io.github.moltenmc.molten.server.tick.TickMetricsSnapshot
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

class MoltenServer(
    val configuration: ServerConfiguration,
    private val tickLoop: ServerTickLoop,
    private val tickMetrics: InMemoryTickMetricsObserver,
    private val scheduledTicks: Boolean = false,
) {
    private val stateRef = AtomicReference(LifecycleState.CREATED)

    val state: LifecycleState
        get() = stateRef.get()

    fun start() {
        if (stateRef.compareAndSet(LifecycleState.CREATED, LifecycleState.STARTING)) {
            if (scheduledTicks) {
                tickLoop.startScheduled(configuration.tickRate)
            } else {
                tickLoop.start()
            }
            stateRef.set(LifecycleState.RUNNING)
        }
    }

    fun tickOnce(): CompletableFuture<ServerTickResult> =
        tickLoop.tickOnce()

    fun tickMetrics(): TickMetricsSnapshot =
        tickMetrics.snapshot()

    fun stop() {
        val current = stateRef.get()
        if (current == LifecycleState.STOPPED || current == LifecycleState.STOPPING) {
            return
        }
        stateRef.set(LifecycleState.STOPPING)
        tickLoop.stop()
        stateRef.set(LifecycleState.STOPPED)
    }

    companion object {
        fun create(
            configuration: ServerConfiguration,
            tickPipeline: TickPipeline = TickPipeline(emptyList()),
        ): MoltenServer {
            val tickMetrics = InMemoryTickMetricsObserver()
            return MoltenServer(
                configuration = configuration,
                tickLoop = ServerTickLoop(
                    pipeline = tickPipeline,
                    observers = listOf(tickMetrics),
                ),
                tickMetrics = tickMetrics,
                scheduledTicks = true,
            )
        }
    }
}

fun main() {
    val server = MoltenServer.create(ServerConfiguration.defaults())
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(server::stop, "molten-shutdown"))
}
