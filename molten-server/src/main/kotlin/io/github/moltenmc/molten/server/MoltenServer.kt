package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.common.world.chunk.WorldChunkService
import io.github.moltenmc.molten.server.config.ServerConfigurationLoader
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition
import io.github.moltenmc.molten.server.tick.InMemoryTickMetricsObserver
import io.github.moltenmc.molten.server.tick.ServerTickLoop
import io.github.moltenmc.molten.server.tick.ServerTickResult
import io.github.moltenmc.molten.server.tick.TickPipeline
import io.github.moltenmc.molten.server.tick.TickTask
import io.github.moltenmc.molten.server.tick.TickMetricsSnapshot
import io.github.moltenmc.molten.server.tick.WorldChunkTickTask
import io.github.moltenmc.molten.server.world.WorldStoragePaths
import io.github.moltenmc.molten.server.world.WorldStorageRuntimeFactory
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class MoltenServer(
    val configuration: ServerConfiguration,
    private val tickLoop: ServerTickLoop,
    private val tickMetrics: InMemoryTickMetricsObserver,
    private val scheduledTicks: Boolean = false,
    private val managedResources: List<AutoCloseable> = emptyList(),
) {
    private val stateRef = AtomicReference(LifecycleState.CREATED)
    private val resourcesClosedRef = AtomicBoolean(false)

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
        closeManagedResources()
        stateRef.set(LifecycleState.STOPPED)
    }

    private fun closeManagedResources() {
        if (!resourcesClosedRef.compareAndSet(false, true)) {
            return
        }

        managedResources.asReversed().forEach { resource ->
            resource.close()
        }
    }

    companion object {
        fun create(
            configuration: ServerConfiguration,
        ): MoltenServer =
            create(
                configuration = configuration,
                runtimeDefinition = RuntimeDefinition.forMode(configuration.runtimeMode),
                worldStoragePaths = WorldStoragePaths(configuration.worldDirectory),
            )

        fun create(
            configuration: ServerConfiguration,
            runtimeDefinition: RuntimeDefinition,
            worldStoragePaths: WorldStoragePaths,
            tickTasks: Iterable<TickTask> = emptyList(),
        ): MoltenServer {
            val worldRuntime = WorldStorageRuntimeFactory(worldStoragePaths).create(runtimeDefinition)
            return create(
                configuration = configuration,
                worldChunks = worldRuntime.chunks,
                tickTasks = tickTasks,
                managedResources = listOf(worldRuntime),
            )
        }

        fun create(
            configuration: ServerConfiguration,
            worldChunks: WorldChunkService? = null,
            tickTasks: Iterable<TickTask> = emptyList(),
            managedResources: List<AutoCloseable> = emptyList(),
        ): MoltenServer =
            create(
                configuration = configuration,
                tickPipeline = TickPipeline(defaultTickTasks(worldChunks, tickTasks)),
                managedResources = managedResources,
            )

        fun create(
            configuration: ServerConfiguration,
            tickPipeline: TickPipeline,
            managedResources: List<AutoCloseable> = emptyList(),
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
                managedResources = managedResources,
            )
        }

        private fun defaultTickTasks(
            worldChunks: WorldChunkService?,
            tickTasks: Iterable<TickTask>,
        ): List<TickTask> =
            buildList {
                if (worldChunks != null) {
                    add(WorldChunkTickTask(worldChunks))
                }
                addAll(tickTasks)
            }
    }
}

fun main() {
    val server = MoltenServer.create(ServerConfigurationLoader(Path.of(".")).loadOrCreate())
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(server::stop, "molten-shutdown"))
}
