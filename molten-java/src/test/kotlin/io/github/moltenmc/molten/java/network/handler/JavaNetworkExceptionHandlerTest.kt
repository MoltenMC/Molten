package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class JavaNetworkExceptionHandlerTest {
    @Test
    fun loginExceptionWritesDisconnectAndClosesChannel() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)
        val channel = EmbeddedChannel(JavaNetworkExceptionHandler(sessionHolder))

        channel.pipeline().fireChannelExceptionCaught(IllegalArgumentException("Invalid login packet."))

        val packet = channel.readOutbound<LoginDisconnectPacket>()
        assertEquals(0x00, packet.packetId)
        assertEquals("{\"text\":\"Invalid login packet.\"}", packet.reasonJson)
        assertEquals(JavaProtocolState.DISCONNECTED, sessionHolder.state)
        assertFalse(channel.isOpen)
    }

    @Test
    fun nonLoginExceptionClosesWithoutPacket() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val channel = EmbeddedChannel(JavaNetworkExceptionHandler(sessionHolder))

        channel.pipeline().fireChannelExceptionCaught(IllegalStateException("Broken play packet."))

        assertEquals(null, channel.readOutbound<LoginDisconnectPacket>())
        assertEquals(JavaProtocolState.DISCONNECTED, sessionHolder.state)
        assertFalse(channel.isOpen)
    }
}
