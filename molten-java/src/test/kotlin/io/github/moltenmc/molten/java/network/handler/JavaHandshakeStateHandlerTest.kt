package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.HandshakeNextState
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.session.JavaProtocolStateHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JavaHandshakeStateHandlerTest {
    @Test
    fun statusHandshakeMovesSessionToStatusState() {
        val stateHolder = JavaProtocolStateHolder()
        val handler = JavaHandshakeStateHandler(stateHolder)
        val packet = handshake(HandshakeNextState.STATUS)

        assertSame(packet, handler.handle(packet))
        assertEquals(JavaProtocolState.STATUS, stateHolder.state)
    }

    @Test
    fun loginHandshakeMovesSessionToLoginState() {
        val stateHolder = JavaProtocolStateHolder()
        val handler = JavaHandshakeStateHandler(stateHolder)
        val packet = handshake(HandshakeNextState.LOGIN)

        assertSame(packet, handler.handle(packet))
        assertEquals(JavaProtocolState.LOGIN, stateHolder.state)
    }

    @Test
    fun nonHandshakePacketPassesThroughWithoutChangingState() {
        val stateHolder = JavaProtocolStateHolder()
        val handler = JavaHandshakeStateHandler(stateHolder)
        val packet = TestPacket(packetId = 0x7f)

        assertSame(packet, handler.handle(packet))
        assertEquals(JavaProtocolState.HANDSHAKE, stateHolder.state)
    }

    private fun handshake(nextState: HandshakeNextState): HandshakePacket =
        HandshakePacket(
            packetId = 0x00,
            protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            serverAddress = "localhost",
            serverPort = 25565,
            nextState = nextState,
        )

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
