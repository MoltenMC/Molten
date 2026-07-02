package io.github.moltenmc.molten.java.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.PlayerChatPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class JavaServerIntentAdapterTest {
    @Test
    fun convertsPlayerChatPacketToIntent() {
        val sessionHolder = JavaSessionHolder().apply {
            playerEntityId = EntityId.of(42, generation = 0, EntityKind.PLAYER)
            currentWorld = WorldId(UUID.fromString("12345678-1234-5678-9abc-def012345678"))
            currentRegion = RegionPos(1, 2)
        }
        val adapter = JavaServerIntentAdapter(sessionHolder)

        val intent = assertIs<ServerIntent.PlayerChat>(
            adapter.toIntent(
                PlayerChatPacket(
                    packetId = 0x07,
                    message = "hello",
                    timestamp = 123L,
                    salt = 456L,
                ),
            ),
        )

        assertEquals(sessionHolder.playerEntityId, intent.sourceEntityId)
        assertEquals(sessionHolder.currentWorld, intent.routing.worldId)
        assertEquals(sessionHolder.currentRegion, intent.routing.regionPos)
        assertEquals("hello", intent.message)
    }

    @Test
    fun returnsNullWhenPlayerEntityIsUnknown() {
        val adapter = JavaServerIntentAdapter(JavaSessionHolder())

        assertNull(
            adapter.toIntent(
                PlayerChatPacket(
                    packetId = 0x07,
                    message = "hello",
                    timestamp = 123L,
                    salt = 456L,
                ),
            ),
        )
    }

    @Test
    fun ignoresUnsupportedPackets() {
        val adapter = JavaServerIntentAdapter(JavaSessionHolder())

        assertNull(adapter.toIntent(TestPacket(packetId = 0x7f)))
    }

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
