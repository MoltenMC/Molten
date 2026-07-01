package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ChunkLoadingCoordinatorTest {
    @Test
    fun returnsAlreadyLoadedChunkWithoutCallingStorage() {
        val request = request()
        val key = ChunkKey.from(request)
        val chunk = Chunk(request.position, emptyList())
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val storage = CountingChunkStorage(CompletableFuture.completedFuture(null))
        val coordinator = coordinator(storage, store)

        val result = coordinator.load(request).get()

        assertIs<ChunkLoadResult.Loaded>(result)
        assertEquals(chunk, result.chunk)
        assertEquals(0, storage.loadCalls)
    }

    @Test
    fun storesChunkLoadedFromStorage() {
        val request = request()
        val key = ChunkKey.from(request)
        val chunk = Chunk(request.position, emptyList())
        val store = LoadedChunkStore()
        val storage = CountingChunkStorage(CompletableFuture.completedFuture(chunk))
        val coordinator = coordinator(storage, store)

        val result = coordinator.load(request).get()

        assertIs<ChunkLoadResult.Loaded>(result)
        assertEquals(chunk, store.get(key))
        assertEquals(1, storage.loadCalls)
    }

    @Test
    fun doesNotStoreMissingChunks() {
        val request = request()
        val key = ChunkKey.from(request)
        val store = LoadedChunkStore()
        val storage = CountingChunkStorage(CompletableFuture.completedFuture(null))
        val coordinator = coordinator(storage, store)

        val result = coordinator.load(request).get()

        assertIs<ChunkLoadResult.Missing>(result)
        assertNull(store.get(key))
    }

    @Test
    fun registersTicketsBeforeLoadCompletes() {
        val request = request()
        val key = ChunkKey.from(request)
        val registry = ChunkTicketRegistry()
        val coordinator = ChunkLoadingCoordinator(
            loadService = ChunkLoadService(CountingChunkStorage(CompletableFuture.completedFuture(null))),
            loadedChunks = LoadedChunkStore(),
            tickets = registry,
        )

        coordinator.load(request).get()

        assertEquals(request.tickets, registry.ticketsFor(key))
    }

    private fun coordinator(
        storage: CountingChunkStorage,
        store: LoadedChunkStore = LoadedChunkStore(),
        registry: ChunkTicketRegistry = ChunkTicketRegistry(),
    ): ChunkLoadingCoordinator =
        ChunkLoadingCoordinator(
            loadService = ChunkLoadService(storage),
            loadedChunks = store,
            tickets = registry,
        )

    private fun request(): ChunkLoadRequest =
        ChunkLoadRequest(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(0, 0),
            tickets = setOf(ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")),
        )

    private class CountingChunkStorage(
        private val loadResult: CompletableFuture<Chunk?>,
    ) : ChunkStorage {
        var loadCalls: Int = 0
            private set

        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> {
            loadCalls++
            return loadResult
        }

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> =
            CompletableFuture.completedFuture(null)
    }
}
