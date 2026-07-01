package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChunkTicketTest {
    @Test
    fun ordersTicketsByPriorityThenTypeThenOwner() {
        val plugin = ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin")
        val player = ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")

        assertEquals(player, setOf(plugin, player).minWith(ChunkTicket.priorityOrder))
    }

    @Test
    fun detectsExpiredTicketsAtExpirationTick() {
        val ticket = ChunkTicket(
            type = ChunkTicketType.TEMPORARY,
            owner = "test",
            expiresAtTick = 20,
        )

        assertFalse(ticket.isExpired(19))
        assertTrue(ticket.isExpired(20))
    }

    @Test
    fun rejectsInvalidTickets() {
        assertFailsWith<IllegalArgumentException> {
            ChunkTicket(ChunkTicketType.PLUGIN, owner = "")
        }
        assertFailsWith<IllegalArgumentException> {
            ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin", radius = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin", priority = -1)
        }
    }

    @Test
    fun loadRequestRequiresTickets() {
        assertFailsWith<IllegalArgumentException> {
            ChunkLoadRequest(
                worldId = WorldId(UUID(0, 1)),
                dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
                position = ChunkPos(0, 0),
                tickets = emptySet(),
            )
        }
    }

    @Test
    fun exposesHighestPriorityTicket() {
        val player = ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")
        val plugin = ChunkTicket(ChunkTicketType.PLUGIN, owner = "plugin")
        val request = ChunkLoadRequest(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(0, 0),
            tickets = setOf(plugin, player),
        )

        assertEquals(player, request.highestPriorityTicket)
    }

    @Test
    fun createsChunkKeyFromLoadRequest() {
        val request = ChunkLoadRequest(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(0, 0),
            tickets = setOf(ChunkTicket(ChunkTicketType.PLAYER_VIEW, owner = "player")),
        )

        assertEquals(
            ChunkKey(
                worldId = request.worldId,
                dimensionId = request.dimensionId,
                position = request.position,
            ),
            ChunkKey.from(request),
        )
    }
}
