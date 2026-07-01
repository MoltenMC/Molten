package io.github.moltenmc.molten.java.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.java.network.codec.JavaHandshakePacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaLoginStartPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaLoginSuccessPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusPingPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusPongPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusRequestPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusResponsePacketCodec
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import io.github.moltenmc.molten.java.network.packet.StatusPingPacket
import io.github.moltenmc.molten.java.network.packet.StatusPongPacket
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JavaPacketRegistriesTest {
    @Test
    fun protocol776RegistersServerboundHandshake() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.HANDSHAKE,
            direction = PacketDirection.SERVERBOUND,
            packetId = JavaHandshakePacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(HandshakePacket::class, entry.packetClass)
    }

    @Test
    fun protocol776RegistersServerboundStatusRequest() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.STATUS,
            direction = PacketDirection.SERVERBOUND,
            packetId = JavaStatusRequestPacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(StatusRequestPacket::class, entry.packetClass)
    }

    @Test
    fun protocol776RegistersClientboundStatusResponse() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.STATUS,
            direction = PacketDirection.CLIENTBOUND,
            packetId = JavaStatusResponsePacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(StatusResponsePacket::class, entry.packetClass)
    }

    @Test
    fun protocol776RegistersServerboundStatusPing() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.STATUS,
            direction = PacketDirection.SERVERBOUND,
            packetId = JavaStatusPingPacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(StatusPingPacket::class, entry.packetClass)
    }

    @Test
    fun protocol776RegistersClientboundStatusPong() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.STATUS,
            direction = PacketDirection.CLIENTBOUND,
            packetId = JavaStatusPongPacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(StatusPongPacket::class, entry.packetClass)
    }

    @Test
    fun protocol776RegistersServerboundLoginStart() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.LOGIN,
            direction = PacketDirection.SERVERBOUND,
            packetId = JavaLoginStartPacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(LoginStartPacket::class, entry.packetClass)
    }

    @Test
    fun protocol776RegistersClientboundLoginSuccess() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.LOGIN,
            direction = PacketDirection.CLIENTBOUND,
            packetId = JavaLoginSuccessPacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(LoginSuccessPacket::class, entry.packetClass)
    }
}
