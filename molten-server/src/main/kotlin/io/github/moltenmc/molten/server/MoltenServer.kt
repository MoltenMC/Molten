package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.common.world.chunk.WorldChunkService
import io.github.moltenmc.molten.server.config.ServerConfigurationLoader
import io.github.moltenmc.molten.server.console.ConsoleServerLogger
import io.github.moltenmc.molten.server.console.ServerLogger
import io.github.moltenmc.molten.server.network.ProtocolListener
import io.github.moltenmc.molten.server.network.ProtocolListenerFactory
import io.github.moltenmc.molten.server.network.ServerIntentInbox
import io.github.moltenmc.molten.server.network.intent.DefaultRegionIntentProcessor
import io.github.moltenmc.molten.server.network.intent.IntentHandlerRegistry
import io.github.moltenmc.molten.server.network.intent.PlayerChatIntentHandler
import io.github.moltenmc.molten.server.network.intent.PlayerMoveIntentHandler
import io.github.moltenmc.molten.server.network.intent.RegionIntentInbox
import io.github.moltenmc.molten.server.network.intent.RegionIntentProcessor
import io.github.moltenmc.molten.server.network.intent.ServerIntentRouter
import io.github.moltenmc.molten.common.ecs.command.EcsCommandBuffer
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition
import io.github.moltenmc.molten.server.tick.InMemoryTickMetricsObserver
import io.github.moltenmc.molten.server.tick.ProtocolListenerTickTask
import io.github.moltenmc.molten.server.tick.RegionIntentSimulationTask
import io.github.moltenmc.molten.server.tick.ServerTickLoop
import io.github.moltenmc.molten.server.tick.ServerTickResult
import io.github.moltenmc.molten.server.tick.ServerIntentDispatchTask
import io.github.moltenmc.molten.server.tick.TickPipeline
import io.github.moltenmc.molten.server.tick.TickPipelineStep
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
    private val logger: ServerLogger = ConsoleServerLogger(),
    private val startupSummary: ServerStartupSummary = ServerStartupSummary.from(configuration),
    private val protocolListeners: List<ProtocolListener> = emptyList(),
) {
    private val stateRef = AtomicReference(LifecycleState.CREATED)
    private val resourcesClosedRef = AtomicBoolean(false)

    val state: LifecycleState
        get() = stateRef.get()

    fun start() {
        if (stateRef.compareAndSet(LifecycleState.CREATED, LifecycleState.STARTING)) {
            val startedListeners = mutableListOf<ProtocolListener>()
            try {
                startupSummary.lines().forEach(logger::info)
                protocolListeners.forEach { listener ->
                    listener.start()
                    startedListeners += listener
                    logger.info("Protocol listener started: ${listener.protocol} ${listener.boundEndpointLabel()}")
                }
                if (scheduledTicks) {
                    tickLoop.startScheduled(configuration.tickRate)
                } else {
                    tickLoop.start()
                }
                logger.info("Tick loop started")
                stateRef.set(LifecycleState.RUNNING)
            } catch (error: Throwable) {
                logger.error("Failed to start Molten server", error)
                rollbackProtocolListeners(startedListeners, error)
                stateRef.set(LifecycleState.STOPPED)
                throw error
            }
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
        stopProtocolListeners()
        closeManagedResources()
        stateRef.set(LifecycleState.STOPPED)
        logger.info("Molten server stopped")
    }

    private fun closeManagedResources() {
        if (!resourcesClosedRef.compareAndSet(false, true)) {
            return
        }

        managedResources.asReversed().forEach { resource ->
            resource.close()
        }
    }

    private fun stopProtocolListeners() {
        protocolListeners.asReversed().forEach { listener ->
            listener.stop()
            logger.info("Protocol listener stopped: ${listener.protocol}")
        }
    }

    private fun rollbackProtocolListeners(
        startedListeners: List<ProtocolListener>,
        startupFailure: Throwable,
    ) {
        startedListeners.asReversed().forEach { listener ->
            try {
                listener.stop()
                logger.info("Protocol listener rolled back: ${listener.protocol}")
            } catch (rollbackFailure: Throwable) {
                startupFailure.addSuppressed(rollbackFailure)
                logger.error("Failed to roll back protocol listener: ${listener.protocol}", rollbackFailure)
            }
        }
    }

    private fun ProtocolListener.boundEndpointLabel(): String =
        boundAddress?.let { "($it)" } ?: "(unbound)"

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
            logger: ServerLogger = ConsoleServerLogger(),
        ): MoltenServer {
            val worldRuntime = WorldStorageRuntimeFactory(worldStoragePaths).create(runtimeDefinition)
            val intentInbox = ServerIntentInbox()
            val regionIntentInbox = RegionIntentInbox()
            
            // Set up intent handler registry with handlers
            val commandBuffer = EcsCommandBuffer()
            val handlerRegistry = IntentHandlerRegistry().apply {
                register(ServerIntent.PlayerMove::class.java, PlayerMoveIntentHandler())
                register(ServerIntent.PlayerChat::class.java, PlayerChatIntentHandler())
                // TODO: Add more intent handlers as they are implemented
            }
            
            // TODO: Provide ComponentReader when entity storage is available
            val componentReader: io.github.moltenmc.molten.server.network.intent.ComponentReader? = null
            
            val regionIntentProcessor = DefaultRegionIntentProcessor(handlerRegistry, commandBuffer, componentReader)
            
            return create(
                configuration = configuration,
                worldChunks = worldRuntime.chunks,
                tickTasks = listOf(
                    ServerIntentDispatchTask(intentInbox, ServerIntentRouter(regionIntentInbox)),
                    RegionIntentSimulationTask(regionIntentInbox, regionIntentProcessor),
                ) + tickTasks,
                managedResources = listOf(worldRuntime),
                logger = logger,
                startupSummary = ServerStartupSummary.from(configuration, worldRuntime.storageKind),
                protocolListeners = ProtocolListenerFactory(configuration, intentInbox).create(runtimeDefinition),
            )
        }

        fun create(
            configuration: ServerConfiguration,
            worldChunks: WorldChunkService? = null,
            tickTasks: Iterable<TickTask> = emptyList(),
            managedResources: List<AutoCloseable> = emptyList(),
            logger: ServerLogger = ConsoleServerLogger(),
            startupSummary: ServerStartupSummary = ServerStartupSummary.from(configuration),
            protocolListeners: List<ProtocolListener> = emptyList(),
        ): MoltenServer =
            create(
                configuration = configuration,
                tickPipeline = TickPipeline(defaultTickTasks(worldChunks, tickTasks, protocolListeners)),
                managedResources = managedResources,
                logger = logger,
                startupSummary = startupSummary,
                protocolListeners = protocolListeners,
            )

        fun create(
            configuration: ServerConfiguration,
            tickPipeline: TickPipeline,
            managedResources: List<AutoCloseable> = emptyList(),
            logger: ServerLogger = ConsoleServerLogger(),
            startupSummary: ServerStartupSummary = ServerStartupSummary.from(configuration),
            protocolListeners: List<ProtocolListener> = emptyList(),
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
                logger = logger,
                startupSummary = startupSummary,
                protocolListeners = protocolListeners,
            )
        }

        private fun defaultTickTasks(
            worldChunks: WorldChunkService?,
            tickTasks: Iterable<TickTask>,
            protocolListeners: List<ProtocolListener>,
        ): List<TickTask> =
            buildList {
                if (worldChunks != null) {
                    add(WorldChunkTickTask(worldChunks))
                }
                if (protocolListeners.isNotEmpty()) {
                    add(
                        ProtocolListenerTickTask(
                            listeners = protocolListeners,
                            step = TickPipelineStep.NETWORK_INGRESS,
                            tickListener = ProtocolListener::tickIngress,
                        ),
                    )
                    add(ProtocolListenerTickTask(protocolListeners))
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
