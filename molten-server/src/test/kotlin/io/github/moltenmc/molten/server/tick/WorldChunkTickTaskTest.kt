package io.github.moltenmc.molten.server.tick

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
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorldChunkTickTaskTest {
    @Test
    fun worldUpdateStepCleansExpiredChunkTickets() {
        val worldId = WorldId(UUID(0, 1))
        val dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld"))
        val position = ChunkPos(2, 3)
        val key = ChunkKey(worldId, dimensionId, position)
        val service = WorldChunkService.create(
            RecordingChunkStorage(CompletableFuture.completedFuture(Chunk(position, emptyList()))),
        )
        service.loadChunk(
            worldId = worldId,
            dimensionId = dimensionId,
            position = position,
            tickets = setOf(
                ChunkTicket(
                    type = ChunkTicketType.TEMPORARY,
                    owner = "temporary",
                    expiresAtTick = 10,
                ),
            ),
        ).get()
        val task = WorldChunkTickTask(service)

        task.execute(currentTick = 10).get()

        assertEquals(emptySet(), service.ticketsFor(key))
        assertNull(service.loadedChunk(key))
    }

    private class RecordingChunkStorage(
        private val loadResult: CompletableFuture<Chunk?>,
    ) : ChunkStorage {
        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
            loadResult

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> =
            CompletableFuture.completedFuture(null)
    }
}
