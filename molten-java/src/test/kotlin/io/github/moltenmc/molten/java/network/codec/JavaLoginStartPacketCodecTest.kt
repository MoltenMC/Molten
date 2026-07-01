package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaLoginStartPacketCodecTest {
    private val codec = JavaLoginStartPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.SERVERBOUND,
    )
    private val uuid = UUID.fromString("12345678-1234-5678-9abc-def012345678")

    @Test
    fun decodesLoginStartPacket() {
        val packet = codec.decode(
            byteArrayOf(
                0x00,
                0x06,
                'P'.code.toByte(),
                'l'.code.toByte(),
                'a'.code.toByte(),
                'y'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
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
            ),
            protocolContext,
        )

        assertEquals(
            LoginStartPacket(
                packetId = 0x00,
                name = "Player",
                playerUuid = uuid,
            ),
            packet,
        )
    }

    @Test
    fun encodesLoginStartPacket() {
        val packet = LoginStartPacket(
            packetId = 0x00,
            name = "Player",
            playerUuid = uuid,
        )

        assertContentEquals(
            byteArrayOf(
                0x00,
                0x06,
                'P'.code.toByte(),
                'l'.code.toByte(),
                'a'.code.toByte(),
                'y'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
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
            ),
            codec.encode(packet, protocolContext),
        )
    }

    @Test
    fun rejectsUsernameLongerThanLimitWhenEncoding() {
        assertFailsWith<IllegalArgumentException> {
            codec.encode(
                LoginStartPacket(
                    packetId = 0x00,
                    name = "a".repeat(JavaLoginStartPacketCodec.MAX_USERNAME_CHARACTERS + 1),
                    playerUuid = uuid,
                ),
                protocolContext,
            )
        }
    }

    @Test
    fun rejectsMissingUuidWhenDecoding() {
        assertFailsWith<IllegalArgumentException> {
            codec.decode(byteArrayOf(0x00, 0x06, 'P'.code.toByte()), protocolContext)
        }
    }

    @Test
    fun validatesLoginStartPacket() {
        val result = codec.validate(
            LoginStartPacket(
                packetId = 0x00,
                name = "Player_1",
                playerUuid = uuid,
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
