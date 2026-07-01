package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaLoginDisconnectPacketCodecTest {
    private val codec = JavaLoginDisconnectPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesLoginDisconnectPacket() {
        val packet = codec.decode(
            byteArrayOf(
                0x00,
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
            ),
            protocolContext,
        )

        assertEquals(LoginDisconnectPacket(packetId = 0x00, reasonJson = "{\"text\":\"Bye\"}"), packet)
    }

    @Test
    fun encodesLoginDisconnectPacket() {
        val packet = LoginDisconnectPacket(packetId = 0x00, reasonJson = "{\"text\":\"Bye\"}")

        assertContentEquals(
            byteArrayOf(
                0x00,
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
            ),
            codec.encode(packet, protocolContext),
        )
    }

    @Test
    fun rejectsTrailingBytes() {
        assertFailsWith<IllegalArgumentException> {
            codec.decode(byteArrayOf(0x00, 0x00, 0x01), protocolContext)
        }
    }

    @Test
    fun validatesLoginDisconnectPacket() {
        val result = codec.validate(
            LoginDisconnectPacket(packetId = 0x00, reasonJson = "{\"text\":\"Bye\"}"),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }
}
