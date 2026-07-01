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

class WorldChunkServiceTest {
    @Test
    fun loadsChunkAndRegistersTickets() {
        val worldId = WorldId(UUID(0, 1))
        val dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld"))
        val position = ChunkPos(3, 4)
        val key = ChunkKey(worldId, dimensionId, position)
        val ticket = ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")
        val chunk = Chunk(position, emptyList())
        val storage = RecordingChunkStorage(loadResult = CompletableFuture.completedFuture(chunk))
        val service = WorldChunkService.create(storage)

        val result = service.loadChunk(worldId, dimensionId, position, setOf(ticket)).get()

        assertIs<ChunkLoadResult.Loaded>(result)
        assertEquals(chunk, service.loadedChunk(key))
        assertEquals(setOf(ticket), service.ticketsFor(key))
    }

    @Test
    fun unloadsLoadedChunkWhenTemporaryTicketExpires() {
        val worldId = WorldId(UUID(0, 1))
        val dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld"))
        val position = ChunkPos(3, 4)
        val key = ChunkKey(worldId, dimensionId, position)
        val ticket = ChunkTicket(
            type = ChunkTicketType.TEMPORARY,
            owner = "temporary",
            expiresAtTick = 5,
        )
        val chunk = Chunk(position, emptyList())
        val storage = RecordingChunkStorage(loadResult = CompletableFuture.completedFuture(chunk))
        val service = WorldChunkService.create(storage)

        service.loadChunk(worldId, dimensionId, position, setOf(ticket)).get()
        val lifecycle = service.cleanupExpiredTickets(currentTick = 5).get()

        assertEquals(setOf(key), lifecycle.unloadCandidates)
        assertIs<ChunkUnloadResult.Unloaded>(lifecycle.unloadResults.single())
        assertNull(service.loadedChunk(key))
    }

    @Test
    fun savesDirtyChunkWhenTemporaryTicketExpires() {
        val worldId = WorldId(UUID(0, 1))
        val dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld"))
        val position = ChunkPos(3, 4)
        val key = ChunkKey(worldId, dimensionId, position)
        val ticket = ChunkTicket(
            type = ChunkTicketType.TEMPORARY,
            owner = "temporary",
            expiresAtTick = 5,
        )
        val chunk = Chunk(
            position = position,
            sections = emptyList(),
            dirtyMarkers = setOf(DirtyMarker.NEEDS_SAVE),
        )
        val storage = RecordingChunkStorage(loadResult = CompletableFuture.completedFuture(chunk))
        val service = WorldChunkService.create(storage)

        service.loadChunk(worldId, dimensionId, position, setOf(ticket)).get()
        val lifecycle = service.cleanupExpiredTickets(currentTick = 5).get()

        val unload = assertIs<ChunkUnloadResult.Unloaded>(lifecycle.unloadResults.single())
        assertEquals(true, unload.saved)
        assertEquals(listOf(chunk), storage.savedChunks)
        assertNull(service.loadedChunk(key))
    }

    private class RecordingChunkStorage(
        private val loadResult: CompletableFuture<Chunk?>,
    ) : ChunkStorage {
        val savedChunks = mutableListOf<Chunk>()

        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
            loadResult

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> {
            savedChunks += chunk
            return CompletableFuture.completedFuture(null)
        }
    }
}
