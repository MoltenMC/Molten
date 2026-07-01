package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaLoginSuccessPacketCodecTest {
    private val codec = JavaLoginSuccessPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )
    private val uuid = UUID.fromString("12345678-1234-5678-9abc-def012345678")

    @Test
    fun decodesLoginSuccessPacket() {
        val packet = codec.decode(
            byteArrayOf(
                0x02,
                0x12,
                0x34,
                0x56,
                0x78,
                0x12,
                0x34,
                0x56,
                0x78,
                0x9a.toByte(),
                0xbc.toByte(),
                0xde.toByte(),
                0xf0.toByte(),
                0x12,
                0x34,
                0x56,
                0x78,
                0x06,
                'P'.code.toByte(),
                'l'.code.toByte(),
                'a'.code.toByte(),
                'y'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
                0x00,
            ),
            protocolContext,
        )

        assertEquals(
            LoginSuccessPacket(
                packetId = 0x02,
                uuid = uuid,
                username = "Player",
            ),
            packet,
        )
    }

    @Test
    fun encodesLoginSuccessPacket() {
        val packet = LoginSuccessPacket(
            packetId = 0x02,
            uuid = uuid,
            username = "Player",
        )

        assertContentEquals(
            byteArrayOf(
                0x02,
                0x12,
                0x34,
                0x56,
                0x78,
                0x12,
                0x34,
                0x56,
                0x78,
                0x9a.toByte(),
                0xbc.toByte(),
                0xde.toByte(),
                0xf0.toByte(),
                0x12,
                0x34,
                0x56,
                0x78,
                0x06,
                'P'.code.toByte(),
                'l'.code.toByte(),
                'a'.code.toByte(),
                'y'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
                0x00,
            ),
            codec.encode(packet, protocolContext),
        )
    }

    @Test
    fun rejectsUsernameLongerThanLimitWhenEncoding() {
        assertFailsWith<IllegalArgumentException> {
            codec.encode(
                LoginSuccessPacket(
                    packetId = 0x02,
                    uuid = uuid,
                    username = "a".repeat(JavaLoginSuccessPacketCodec.MAX_USERNAME_CHARACTERS + 1),
                ),
                protocolContext,
            )
        }
    }

    @Test
    fun validatesLoginSuccessPacket() {
        val result = codec.validate(
            LoginSuccessPacket(
                packetId = 0x02,
                uuid = uuid,
                username = "Player",
            ),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }
}
