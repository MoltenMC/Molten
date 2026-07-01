package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.codec.JavaLoginStartPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaPacketDecoder
import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.buffer.BufferAllocator
import io.netty5.channel.embedded.EmbeddedChannel
import java.util.UUID
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

    @Test
    fun loginValidationFailureWritesDisconnectAndClosesChannel() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)
        val channel = EmbeddedChannel(
            JavaPacketDecoder(stateHolder = sessionHolder),
            JavaNetworkExceptionHandler(sessionHolder),
        )
        val packet = LoginStartPacket(
            packetId = JavaLoginStartPacketCodec.PACKET_ID,
            name = "bad-name",
            playerUuid = UUID.fromString("12345678-1234-5678-9abc-def012345678"),
        )
        val bytes = JavaLoginStartPacketCodec().encode(
            packet,
            ProtocolContext(
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
                direction = PacketDirection.SERVERBOUND,
            ),
        )

        BufferAllocator.onHeapUnpooled().use { allocator ->
            channel.writeInbound(allocator.copyOf(bytes))
        }

        val disconnect = channel.readOutbound<LoginDisconnectPacket>()
        assertEquals(
            "{\"text\":\"Login username contains invalid characters.\"}",
            disconnect.reasonJson,
        )
        assertEquals(JavaProtocolState.DISCONNECTED, sessionHolder.state)
        assertFalse(channel.isOpen)
    }
}
