package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaStatusPacketCodecTest {
    private val serverboundContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.SERVERBOUND,
    )
    private val clientboundContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesStatusRequestPacket() {
        val packet = JavaStatusRequestPacketCodec().decode(byteArrayOf(0x00), serverboundContext)

        assertEquals(StatusRequestPacket(packetId = 0x00), packet)
    }

    @Test
    fun encodesStatusRequestPacket() {
        val packet = StatusRequestPacket(packetId = 0x00)

        assertContentEquals(byteArrayOf(0x00), JavaStatusRequestPacketCodec().encode(packet, serverboundContext))
    }

    @Test
    fun rejectsStatusRequestTrailingBytes() {
        assertFailsWith<IllegalArgumentException> {
            JavaStatusRequestPacketCodec().decode(byteArrayOf(0x00, 0x01), serverboundContext)
        }
    }

    @Test
    fun decodesStatusResponsePacket() {
        val payload = byteArrayOf(
            0x00,
            0x0f,
            '{'.code.toByte(),
            '"'.code.toByte(),
            'v'.code.toByte(),
            'e'.code.toByte(),
            'r'.code.toByte(),
            's'.code.toByte(),
            'i'.code.toByte(),
            'o'.code.toByte(),
            'n'.code.toByte(),
            '"'.code.toByte(),
            ':'.code.toByte(),
            '"'.code.toByte(),
            'x'.code.toByte(),
            '"'.code.toByte(),
            '}'.code.toByte(),
        )

        val packet = JavaStatusResponsePacketCodec().decode(payload, clientboundContext)

        assertEquals(StatusResponsePacket(packetId = 0x00, json = "{\"version\":\"x\"}"), packet)
    }

    @Test
    fun encodesStatusResponsePacket() {
        val packet = StatusResponsePacket(packetId = 0x00, json = "{\"version\":\"x\"}")

        assertContentEquals(
            byteArrayOf(
                0x00,
                0x0f,
                '{'.code.toByte(),
                '"'.code.toByte(),
                'v'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
                's'.code.toByte(),
                'i'.code.toByte(),
                'o'.code.toByte(),
                'n'.code.toByte(),
                '"'.code.toByte(),
                ':'.code.toByte(),
                '"'.code.toByte(),
                'x'.code.toByte(),
                '"'.code.toByte(),
                '}'.code.toByte(),
            ),
            JavaStatusResponsePacketCodec().encode(packet, clientboundContext),
        )
    }

    @Test
    fun validatesStatusResponsePacket() {
        val result = JavaStatusResponsePacketCodec().validate(
            StatusResponsePacket(packetId = 0x00, json = "{}"),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }
}
