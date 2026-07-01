package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChunkTicketRegistryTest {
    @Test
    fun storesMultipleTicketsForSameChunk() {
        val registry = ChunkTicketRegistry()
        val key = key()
        val player = ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")
        val plugin = ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin")

        registry.addTicket(key, player)
        registry.addTicket(key, plugin)

        assertEquals(setOf(player, plugin), registry.ticketsFor(key))
    }

    @Test
    fun removesSpecificTicketAndDropsEmptyChunkEntry() {
        val registry = ChunkTicketRegistry()
        val key = key()
        val ticket = ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin")

        registry.addTicket(key, ticket)

        assertTrue(registry.removeTicket(key, ticket))
        assertFalse(registry.removeTicket(key, ticket))
        assertEquals(emptySet(), registry.ticketsFor(key))
    }

    @Test
    fun cleanupExpiredTicketsReturnsUnloadCandidates() {
        val registry = ChunkTicketRegistry()
        val key = key()
        registry.addTicket(
            key,
            ChunkTicket(
                type = ChunkTicketType.TEMPORARY,
                owner = "temporary",
                expiresAtTick = 10,
            ),
        )

        assertEquals(emptySet(), registry.cleanupExpired(9))
        assertEquals(setOf(key), registry.cleanupExpired(10))
        assertEquals(emptySet(), registry.ticketsFor(key))
    }

    @Test
    fun cleanupKeepsChunkWhenAtLeastOneTicketRemains() {
        val registry = ChunkTicketRegistry()
        val key = key()
        val persistent = ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin")
        registry.addTicket(key, persistent)
        registry.addTicket(
            key,
            ChunkTicket(
                type = ChunkTicketType.TEMPORARY,
                owner = "temporary",
                expiresAtTick = 10,
            ),
        )

        assertEquals(emptySet(), registry.cleanupExpired(10))
        assertEquals(setOf(persistent), registry.ticketsFor(key))
    }

    private fun key(): ChunkKey =
        ChunkKey(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(1, 2),
        )
}
