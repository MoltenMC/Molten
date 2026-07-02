package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.packet.AcknowledgeFinishConfigurationPacket
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.JavaPlayJoinPacket
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class JavaConfigurationFinishHandlerTest {
    @Test
    fun acknowledgementMovesSessionToPlayState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)
        val handler = JavaConfigurationFinishHandler(sessionHolder)
        val packet = AcknowledgeFinishConfigurationPacket(packetId = 0x03)

        val result = assertIs<JavaConfigurationFinishHandler.ConfigurationFinishResult>(handler.handle(packet))

        assertEquals(1, result.playPackets.size)
        assertEquals(0x2b, assertIs<JavaPlayJoinPacket>(result.playPackets.single()).packetId)
        assertEquals(JavaProtocolState.PLAY, sessionHolder.state)
    }

    @Test
    fun nonAcknowledgementPacketPassesThroughWithoutChangingState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)
        val handler = JavaConfigurationFinishHandler(sessionHolder)
        val packet = TestPacket(packetId = 0x7f)

        assertSame(packet, handler.handle(packet))
        assertEquals(JavaProtocolState.CONFIGURATION, sessionHolder.state)
    }

    @Test
    fun acknowledgementFlushesQueuedMessagesAfterPlayPackets() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)
        val handler = JavaConfigurationFinishHandler(sessionHolder)
        val channel = EmbeddedChannel(handler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Welcome")))

        channel.writeInbound(AcknowledgeFinishConfigurationPacket(packetId = 0x03))

        assertEquals(0, sessionHolder.outboundQueue.size)
        assertEquals(0x2b, assertIs<JavaPlayJoinPacket>(channel.readOutbound()).packetId)
        val chatPacket = assertIs<SystemChatPacket>(channel.readOutbound())
        assertEquals("{\"text\":\"Welcome\"}", chatPacket.contentJson)
        assertEquals(JavaProtocolState.PLAY, sessionHolder.state)
    }

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
