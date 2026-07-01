package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaSystemChatPacketCodecTest {
    private val codec = JavaSystemChatPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesSystemChatPacket() {
        val packet = codec.decode(systemChatBytes(overlay = true), protocolContext)

        assertEquals(
            SystemChatPacket(
                packetId = JavaSystemChatPacketCodec.PACKET_ID,
                contentJson = "{\"text\":\"Hi\"}",
                overlay = true,
            ),
            packet,
        )
    }

    @Test
    fun encodesSystemChatPacket() {
        val packet = SystemChatPacket(
            packetId = JavaSystemChatPacketCodec.PACKET_ID,
            contentJson = "{\"text\":\"Hi\"}",
            overlay = false,
        )

        assertContentEquals(systemChatBytes(overlay = false), codec.encode(packet, protocolContext))
    }

    @Test
    fun rejectsTrailingBytes() {
        assertFailsWith<IllegalArgumentException> {
            codec.decode(systemChatBytes(overlay = false) + byteArrayOf(0x01), protocolContext)
        }
    }

    @Test
    fun validatesSystemChatPacket() {
        val result = codec.validate(
            SystemChatPacket(
                packetId = JavaSystemChatPacketCodec.PACKET_ID,
                contentJson = "{\"text\":\"Hi\"}",
                overlay = false,
            ),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }

    private fun systemChatBytes(overlay: Boolean): ByteArray =
        byteArrayOf(
            JavaSystemChatPacketCodec.PACKET_ID.toByte(),
            0x0d,
            '{'.code.toByte(),
            '"'.code.toByte(),
            't'.code.toByte(),
            'e'.code.toByte(),
            'x'.code.toByte(),
            't'.code.toByte(),
            '"'.code.toByte(),
            ':'.code.toByte(),
            '"'.code.toByte(),
            'H'.code.toByte(),
            'i'.code.toByte(),
            '"'.code.toByte(),
            '}'.code.toByte(),
            if (overlay) 0x01 else 0x00,
        )
}
