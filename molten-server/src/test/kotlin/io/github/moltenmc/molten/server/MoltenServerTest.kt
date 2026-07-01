package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import io.github.moltenmc.molten.common.world.chunk.Chunk
import io.github.moltenmc.molten.common.world.chunk.ChunkKey
import io.github.moltenmc.molten.common.world.chunk.ChunkStorage
import io.github.moltenmc.molten.common.world.chunk.ChunkTicket
import io.github.moltenmc.molten.common.world.chunk.ChunkTicketType
import io.github.moltenmc.molten.common.world.chunk.WorldChunkService
import io.github.moltenmc.molten.server.console.ServerLogger
import io.github.moltenmc.molten.server.network.ProtocolListener
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition
import io.github.moltenmc.molten.server.runtime.RuntimeMode
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import io.github.moltenmc.molten.server.tick.InMemoryTickMetricsObserver
import io.github.moltenmc.molten.server.tick.ServerTickLoop
import io.github.moltenmc.molten.server.tick.TickRate
import io.github.moltenmc.molten.server.tick.TickTask
import io.github.moltenmc.molten.server.tick.TickPipeline
import io.github.moltenmc.molten.server.tick.TickPipelineStep
import io.github.moltenmc.molten.server.world.WorldStoragePaths
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoltenServerTest {
    @Test
    fun defaultConfigurationUsesJavaBasedRuntimeAndWorldDirectory() {
        val configuration = ServerConfiguration.defaults()

        assertEquals(RuntimeMode.JAVA_BASED, configuration.runtimeMode)
        assertEquals(Path.of("world"), configuration.worldDirectory)
    }

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

    @Test
    fun createdServerIncludesWorldChunkTickTaskWhenChunksAreProvided() {
        val worldId = WorldId(UUID(0, 1))
        val dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld"))
        val position = ChunkPos(2, 3)
        val key = ChunkKey(worldId, dimensionId, position)
        val worldChunks = WorldChunkService.create(
            RecordingChunkStorage(CompletableFuture.completedFuture(Chunk(position, emptyList()))),
        )
        val latch = CountDownLatch(1)
        val server = MoltenServer.create(
            configuration = ServerConfiguration.defaults().copy(tickRate = TickRate(100)),
            worldChunks = worldChunks,
            tickTasks = listOf(LatchTask(latch)),
        )

        worldChunks.loadChunk(
            worldId = worldId,
            dimensionId = dimensionId,
            position = position,
            tickets = setOf(
                ChunkTicket(
                    type = ChunkTicketType.TEMPORARY,
                    owner = "temporary",
                    expiresAtTick = 0,
                ),
            ),
        ).get()

        try {
            server.start()

            assertTrue(latch.await(1, TimeUnit.SECONDS))
            assertNull(worldChunks.loadedChunk(key))
        } finally {
            server.stop()
        }
    }

    @Test
    fun closesManagedResourcesWhenStopped() {
        val closeCount = AtomicInteger(0)
        val server = MoltenServer.create(
            configuration = ServerConfiguration.defaults().copy(tickRate = TickRate(100)),
            tickPipeline = TickPipeline(emptyList()),
            managedResources = listOf(AutoCloseable { closeCount.incrementAndGet() }),
        )

        server.start()
        server.stop()
        server.stop()

        assertEquals(1, closeCount.get())
    }

    @Test
    fun createsServerFromRuntimeDefinitionAndWorldStoragePaths() {
        withTempDirectory { directory ->
            val paths = WorldStoragePaths(directory)
            val latch = CountDownLatch(1)
            val server = MoltenServer.create(
                configuration = ServerConfiguration.defaults().copy(
                    javaPort = freePort(),
                    tickRate = TickRate(100),
                ),
                runtimeDefinition = RuntimeDefinition.forMode(RuntimeMode.JAVA_ONLY),
                worldStoragePaths = paths,
                tickTasks = listOf(LatchTask(latch)),
            )

            try {
                server.start()

                assertTrue(latch.await(1, TimeUnit.SECONDS))
                assertTrue(Files.isDirectory(paths.javaRegionDirectory))
            } finally {
                server.stop()
            }
        }
    }

    @Test
    fun createsServerFromConfigurationRuntimeAndWorldDirectory() {
        withTempDirectory { directory ->
            val configuration = ServerConfiguration.defaults().copy(
                javaPort = freePort(),
                tickRate = TickRate(100),
                runtimeMode = RuntimeMode.JAVA_ONLY,
                worldDirectory = directory,
            )
            val server = MoltenServer.create(configuration)

            try {
                server.start()

                assertTrue(Files.isDirectory(directory.resolve("region")))
            } finally {
                server.stop()
            }
        }
    }

    @Test
    fun logsStartupSummaryWhenServerStarts() {
        val logger = RecordingLogger()
        val server = MoltenServer.create(
            configuration = ServerConfiguration.defaults().copy(tickRate = TickRate(100)),
            tickPipeline = TickPipeline(emptyList()),
            logger = logger,
        )

        server.start()
        server.stop()

        assertTrue(logger.infoMessages.contains("Starting Molten server"))
        assertTrue(logger.infoMessages.contains("Runtime mode: JAVA_BASED"))
        assertTrue(logger.infoMessages.contains("World storage: unconfigured"))
        assertTrue(logger.infoMessages.contains("Tick loop started"))
        assertTrue(logger.infoMessages.contains("Molten server stopped"))
    }

    @Test
    fun logsRuntimeStorageKindInStartupSummary() {
        withTempDirectory { directory ->
            val logger = RecordingLogger()
            val server = MoltenServer.create(
                configuration = ServerConfiguration.defaults().copy(
                    javaPort = freePort(),
                    tickRate = TickRate(100),
                ),
                runtimeDefinition = RuntimeDefinition.forMode(RuntimeMode.JAVA_ONLY),
                worldStoragePaths = WorldStoragePaths(directory),
                logger = logger,
            )

            try {
                server.start()

                assertTrue(logger.infoMessages.contains("World storage: JAVA_ANVIL"))
            } finally {
                server.stop()
            }
        }
    }

    @Test
    fun startsAndStopsProtocolListenersWithServer() {
        val logger = RecordingLogger()
        val listener = RecordingProtocolListener(ProtocolStack.JAVA_EDITION)
        val server = MoltenServer.create(
            configuration = ServerConfiguration.defaults().copy(tickRate = TickRate(100)),
            tickPipeline = TickPipeline(emptyList()),
            logger = logger,
            protocolListeners = listOf(listener),
        )

        server.start()

        assertTrue(listener.isRunning)
        assertEquals(1, listener.startCalls)
        assertTrue(logger.infoMessages.contains("Protocol listener started: JAVA_EDITION (127.0.0.1:25565)"))

        server.stop()

        assertFalse(listener.isRunning)
        assertEquals(1, listener.stopCalls)
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

    private class RecordingChunkStorage(
        private val loadResult: CompletableFuture<Chunk?>,
    ) : ChunkStorage {
        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
            loadResult

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> =
            CompletableFuture.completedFuture(null)
    }

    private class RecordingLogger : ServerLogger {
        val infoMessages = mutableListOf<String>()

        override fun info(message: String) {
            infoMessages += message
        }

        override fun warn(message: String) {
        }

        override fun error(message: String, cause: Throwable?) {
        }
    }

    private class RecordingProtocolListener(
        override val protocol: ProtocolStack,
    ) : ProtocolListener {
        var startCalls: Int = 0
        var stopCalls: Int = 0

        override var isRunning: Boolean = false
            private set

        override val boundAddress: String
            get() = "127.0.0.1:25565"

        override fun start() {
            startCalls++
            isRunning = true
        }

        override fun stop() {
            stopCalls++
            isRunning = false
        }
    }

    private fun withTempDirectory(block: (Path) -> Unit) {
        val directory = Files.createTempDirectory("molten-server-test")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private fun freePort(): Int =
        ServerSocket(0).use { socket -> socket.localPort }
}
