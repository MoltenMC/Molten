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

class ChunkLoadServiceTest {
    @Test
    fun returnsLoadedResultWhenStorageFindsChunk() {
        val request = request()
        val chunk = Chunk(request.position, emptyList())
        val service = ChunkLoadService(FakeChunkStorage(CompletableFuture.completedFuture(chunk)))

        val result = service.load(request).get()

        assertIs<ChunkLoadResult.Loaded>(result)
        assertEquals(request, result.request)
        assertEquals(chunk, result.chunk)
    }

    @Test
    fun returnsMissingResultWhenStorageDoesNotFindChunk() {
        val request = request()
        val service = ChunkLoadService(FakeChunkStorage(CompletableFuture.completedFuture(null)))

        val result = service.load(request).get()

        assertIs<ChunkLoadResult.Missing>(result)
        assertEquals(request, result.request)
    }

    @Test
    fun returnsFailedResultWhenStorageFails() {
        val request = request()
        val failure = IllegalStateException("load failed")
        val service = ChunkLoadService(FakeChunkStorage(CompletableFuture.failedFuture(failure)))

        val result = service.load(request).get()

        assertIs<ChunkLoadResult.Failed>(result)
        assertEquals(request, result.request)
        assertEquals(failure, result.cause)
    }

    private fun request(): ChunkLoadRequest =
        ChunkLoadRequest(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(0, 0),
            tickets = setOf(ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")),
        )

    private class FakeChunkStorage(
        private val loadResult: CompletableFuture<Chunk?>,
    ) : ChunkStorage {
        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
            loadResult

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> =
            CompletableFuture.completedFuture(null)
    }
}
