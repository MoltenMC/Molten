package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.StatusPingPacket
import io.github.moltenmc.molten.java.network.packet.StatusPongPacket
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaStatusPingPongPacketCodecTest {
    private val serverboundContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.SERVERBOUND,
    )
    private val clientboundContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesStatusPingPacket() {
        val packet = JavaStatusPingPacketCodec().decode(
            byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x07, 0x5b, 0xcd.toByte(), 0x15),
            serverboundContext,
        )

        assertEquals(StatusPingPacket(packetId = 0x01, payload = 123456789), packet)
    }

    @Test
    fun encodesStatusPingPacket() {
        val packet = StatusPingPacket(packetId = 0x01, payload = 123456789)

        assertContentEquals(
            byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x07, 0x5b, 0xcd.toByte(), 0x15),
            JavaStatusPingPacketCodec().encode(packet, serverboundContext),
        )
    }

    @Test
    fun decodesStatusPongPacket() {
        val packet = JavaStatusPongPacketCodec().decode(
            byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x07, 0x5b, 0xcd.toByte(), 0x15),
            clientboundContext,
        )

        assertEquals(StatusPongPacket(packetId = 0x01, payload = 123456789), packet)
    }

    @Test
    fun encodesStatusPongPacket() {
        val packet = StatusPongPacket(packetId = 0x01, payload = 123456789)

        assertContentEquals(
            byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x07, 0x5b, 0xcd.toByte(), 0x15),
            JavaStatusPongPacketCodec().encode(packet, clientboundContext),
        )
    }

    @Test
    fun rejectsMissingPingPayload() {
        assertFailsWith<IllegalArgumentException> {
            JavaStatusPingPacketCodec().decode(byteArrayOf(0x01), serverboundContext)
        }
    }
}
