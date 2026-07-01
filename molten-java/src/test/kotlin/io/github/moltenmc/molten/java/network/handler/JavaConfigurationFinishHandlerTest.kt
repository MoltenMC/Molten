package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.AcknowledgeFinishConfigurationPacket
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JavaConfigurationFinishHandlerTest {
    @Test
    fun acknowledgementMovesSessionToPlayState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)
        val handler = JavaConfigurationFinishHandler(sessionHolder)
        val packet = AcknowledgeFinishConfigurationPacket(packetId = 0x03)

        assertSame(packet, handler.handle(packet))
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

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
