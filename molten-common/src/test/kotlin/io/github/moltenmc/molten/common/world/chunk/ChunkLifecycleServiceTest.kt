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

class ChunkLifecycleServiceTest {
    @Test
    fun doesNothingWhenNoTicketsExpire() {
        val key = key()
        val registry = ChunkTicketRegistry().apply {
            addTicket(key, ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin"))
        }
        val store = LoadedChunkStore().apply { put(key, Chunk(key.position, emptyList())) }
        val storage = RecordingChunkStorage()
        val service = lifecycleService(registry, storage, store)

        val result = service.cleanupExpiredTickets(currentTick = 10).get()

        assertEquals(emptySet(), result.unloadCandidates)
        assertEquals(emptyList(), result.unloadResults)
        assertEquals(0, storage.savedChunks.size)
        assertEquals(Chunk(key.position, emptyList()), store.get(key))
    }

    @Test
    fun unloadsCleanChunkWhenLastTicketExpires() {
        val key = key()
        val registry = ChunkTicketRegistry().apply {
            addTicket(
                key,
                ChunkTicket(
                    type = ChunkTicketType.TEMPORARY,
                    owner = "temporary",
                    expiresAtTick = 10,
                ),
            )
        }
        val store = LoadedChunkStore().apply { put(key, Chunk(key.position, emptyList())) }
        val service = lifecycleService(registry, RecordingChunkStorage(), store)

        val result = service.cleanupExpiredTickets(currentTick = 10).get()

        assertEquals(setOf(key), result.unloadCandidates)
        val unload = assertIs<ChunkUnloadResult.Unloaded>(result.unloadResults.single())
        assertEquals(false, unload.saved)
        assertNull(store.get(key))
    }

    @Test
    fun savesDirtyChunkBeforeUnloadWhenLastTicketExpires() {
        val key = key()
        val chunk = Chunk(
            position = key.position,
            sections = emptyList(),
            dirtyMarkers = setOf(DirtyMarker.NEEDS_SAVE),
        )
        val registry = ChunkTicketRegistry().apply {
            addTicket(
                key,
                ChunkTicket(
                    type = ChunkTicketType.TEMPORARY,
                    owner = "temporary",
                    expiresAtTick = 10,
                ),
            )
        }
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val storage = RecordingChunkStorage()
        val service = lifecycleService(registry, storage, store)

        val result = service.cleanupExpiredTickets(currentTick = 10).get()

        val unload = assertIs<ChunkUnloadResult.Unloaded>(result.unloadResults.single())
        assertEquals(true, unload.saved)
        assertEquals(listOf(chunk), storage.savedChunks)
        assertNull(store.get(key))
    }

    @Test
    fun keepsLoadedChunkWhenPersistentTicketRemains() {
        val key = key()
        val persistent = ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin")
        val chunk = Chunk(key.position, emptyList())
        val registry = ChunkTicketRegistry().apply {
            addTicket(key, persistent)
            addTicket(
                key,
                ChunkTicket(
                    type = ChunkTicketType.TEMPORARY,
                    owner = "temporary",
                    expiresAtTick = 10,
                ),
            )
        }
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val service = lifecycleService(registry, RecordingChunkStorage(), store)

        val result = service.cleanupExpiredTickets(currentTick = 10).get()

        assertEquals(emptySet(), result.unloadCandidates)
        assertEquals(setOf(persistent), registry.ticketsFor(key))
        assertEquals(chunk, store.get(key))
    }

    @Test
    fun reportsSaveFailureAndKeepsChunkLoaded() {
        val key = key()
        val failure = IllegalStateException("save failed")
        val chunk = Chunk(
            position = key.position,
            sections = emptyList(),
            dirtyMarkers = setOf(DirtyMarker.NEEDS_SAVE),
        )
        val registry = ChunkTicketRegistry().apply {
            addTicket(
                key,
                ChunkTicket(
                    type = ChunkTicketType.TEMPORARY,
                    owner = "temporary",
                    expiresAtTick = 10,
                ),
            )
        }
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val storage = RecordingChunkStorage(saveResult = CompletableFuture.failedFuture(failure))
        val service = lifecycleService(registry, storage, store)

        val result = service.cleanupExpiredTickets(currentTick = 10).get()

        val unload = assertIs<ChunkUnloadResult.Failed>(result.unloadResults.single())
        assertEquals(failure, unload.cause)
        assertEquals(chunk, store.get(key))
    }

    private fun lifecycleService(
        registry: ChunkTicketRegistry,
        storage: RecordingChunkStorage,
        store: LoadedChunkStore,
    ): ChunkLifecycleService =
        ChunkLifecycleService(
            tickets = registry,
            unloadService = ChunkUnloadService(storage, store),
        )

    private fun key(): ChunkKey =
        ChunkKey(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(1, 2),
        )

    private class RecordingChunkStorage(
        private val saveResult: CompletableFuture<Void> = CompletableFuture.completedFuture(null),
    ) : ChunkStorage {
        val savedChunks = mutableListOf<Chunk>()

        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
            CompletableFuture.completedFuture(null)

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> {
            savedChunks += chunk
            return saveResult
        }
    }
}
