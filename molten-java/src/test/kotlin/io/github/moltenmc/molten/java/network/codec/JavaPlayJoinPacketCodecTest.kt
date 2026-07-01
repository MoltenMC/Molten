package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.JavaPlayJoinPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaPlayJoinPacketCodecTest {
    private val codec = JavaPlayJoinPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesPlayJoinPacketSkeleton() {
        val bytes = byteArrayOf(
            0x2b,
            0x00,
            0x00,
            0x00,
            0x01,
            0x00,
            0x00,
            0xff.toByte(),
            0x13,
            'm'.code.toByte(),
            'i'.code.toByte(),
            'n'.code.toByte(),
            'e'.code.toByte(),
            'c'.code.toByte(),
            'r'.code.toByte(),
            'a'.code.toByte(),
            'f'.code.toByte(),
            't'.code.toByte(),
            ':'.code.toByte(),
            'o'.code.toByte(),
            'v'.code.toByte(),
            'e'.code.toByte(),
            'r'.code.toByte(),
            'w'.code.toByte(),
            'o'.code.toByte(),
            'r'.code.toByte(),
            'l'.code.toByte(),
            'd'.code.toByte(),
            0x13,
            'm'.code.toByte(),
            'i'.code.toByte(),
            'n'.code.toByte(),
            'e'.code.toByte(),
            'c'.code.toByte(),
            'r'.code.toByte(),
            'a'.code.toByte(),
            'f'.code.toByte(),
            't'.code.toByte(),
            ':'.code.toByte(),
            'o'.code.toByte(),
            'v'.code.toByte(),
            'e'.code.toByte(),
            'r'.code.toByte(),
            'w'.code.toByte(),
            'o'.code.toByte(),
            'r'.code.toByte(),
            'l'.code.toByte(),
            'd'.code.toByte(),
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
        )

        val packet = codec.decode(bytes, protocolContext)

        assertEquals(playJoinPacket(), packet)
    }

    @Test
    fun encodesPlayJoinPacketSkeleton() {
        val encoded = codec.encode(playJoinPacket(), protocolContext)

        assertContentEquals(
            byteArrayOf(
                0x2b,
                0x00,
                0x00,
                0x00,
                0x01,
                0x00,
                0x00,
                0xff.toByte(),
                0x13,
                'm'.code.toByte(),
                'i'.code.toByte(),
                'n'.code.toByte(),
                'e'.code.toByte(),
                'c'.code.toByte(),
                'r'.code.toByte(),
                'a'.code.toByte(),
                'f'.code.toByte(),
                't'.code.toByte(),
                ':'.code.toByte(),
                'o'.code.toByte(),
                'v'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
                'w'.code.toByte(),
                'o'.code.toByte(),
                'r'.code.toByte(),
                'l'.code.toByte(),
                'd'.code.toByte(),
                0x13,
                'm'.code.toByte(),
                'i'.code.toByte(),
                'n'.code.toByte(),
                'e'.code.toByte(),
                'c'.code.toByte(),
                'r'.code.toByte(),
                'a'.code.toByte(),
                'f'.code.toByte(),
                't'.code.toByte(),
                ':'.code.toByte(),
                'o'.code.toByte(),
                'v'.code.toByte(),
                'e'.code.toByte(),
                'r'.code.toByte(),
                'w'.code.toByte(),
                'o'.code.toByte(),
                'r'.code.toByte(),
                'l'.code.toByte(),
                'd'.code.toByte(),
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
            ),
            encoded,
        )
    }

    @Test
    fun rejectsTrailingBytes() {
        assertFailsWith<IllegalArgumentException> {
            codec.decode(byteArrayOf(0x2b, 0x00), protocolContext)
        }
    }

    @Test
    fun validatesPlayJoinPacketSkeleton() {
        val result = codec.validate(
            playJoinPacket(),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }

    private fun playJoinPacket(): JavaPlayJoinPacket =
        JavaPlayJoinPacket(
            packetId = 0x2b,
            entityId = 1,
            hardcore = false,
            gameMode = 0,
            previousGameMode = -1,
            dimensionType = "minecraft:overworld",
            worldName = "minecraft:overworld",
            hashedSeed = 0L,
        )
}
