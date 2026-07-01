package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.ConfigurationDisconnectPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaConfigurationDisconnectPacketCodecTest {
    private val codec = JavaConfigurationDisconnectPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesConfigurationDisconnectPacket() {
        val packet = codec.decode(disconnectBytes(packetId = 0x02), protocolContext)

        assertEquals(ConfigurationDisconnectPacket(packetId = 0x02, reasonJson = "{\"text\":\"Bye\"}"), packet)
    }

    @Test
    fun encodesConfigurationDisconnectPacket() {
        val packet = ConfigurationDisconnectPacket(packetId = 0x02, reasonJson = "{\"text\":\"Bye\"}")

        assertContentEquals(disconnectBytes(packetId = 0x02), codec.encode(packet, protocolContext))
    }

    @Test
    fun rejectsTrailingBytes() {
        assertFailsWith<IllegalArgumentException> {
            codec.decode(byteArrayOf(0x02, 0x00, 0x01), protocolContext)
        }
    }

    @Test
    fun validatesConfigurationDisconnectPacket() {
        val result = codec.validate(
            ConfigurationDisconnectPacket(packetId = 0x02, reasonJson = "{\"text\":\"Bye\"}"),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }

    private fun disconnectBytes(packetId: Int): ByteArray =
        byteArrayOf(
            packetId.toByte(),
            0x0e,
            '{'.code.toByte(),
            '"'.code.toByte(),
            't'.code.toByte(),
            'e'.code.toByte(),
            'x'.code.toByte(),
            't'.code.toByte(),
            '"'.code.toByte(),
            ':'.code.toByte(),
            '"'.code.toByte(),
            'B'.code.toByte(),
            'y'.code.toByte(),
            'e'.code.toByte(),
            '"'.code.toByte(),
            '}'.code.toByte(),
        )
}
