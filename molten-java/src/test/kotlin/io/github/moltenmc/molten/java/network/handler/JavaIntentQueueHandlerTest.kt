package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.PlayerChatPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JavaIntentQueueHandlerTest {
    @Test
    fun queuesSupportedPacketIntent() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY).apply {
            playerEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        }
        val handler = JavaIntentQueueHandler(sessionHolder)
        val packet = PlayerChatPacket(packetId = 0x07, message = "hello", timestamp = 1L, salt = 2L)

        assertTrue(handler.handle(packet))

        val intent = assertIs<ServerIntent.PlayerChat>(sessionHolder.inboundIntentQueue.drain().single())
        assertEquals("hello", intent.message)
    }

    @Test
    fun passesUnsupportedPacketThrough() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val handler = JavaIntentQueueHandler(sessionHolder)
        val channel = EmbeddedChannel(handler)
        val packet = TestPacket(packetId = 0x7f)

        channel.writeInbound(packet)

        assertSame(packet, channel.readInbound())
        assertEquals(0, sessionHolder.inboundIntentQueue.size)
    }

    @Test
    fun returnsFalseWhenIntentCannotBeCreated() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val handler = JavaIntentQueueHandler(sessionHolder)

        assertFalse(
            handler.handle(
                PlayerChatPacket(packetId = 0x07, message = "hello", timestamp = 1L, salt = 2L),
            ),
        )
        assertEquals(0, sessionHolder.inboundIntentQueue.size)
    }

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
