package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.HandshakeNextState
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaHandshakePacketCodecTest {
    private val codec = JavaHandshakePacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = 776,
        direction = PacketDirection.SERVERBOUND,
    )

    @Test
    fun decodesHandshakePacket() {
        val packet = codec.decode(
            byteArrayOf(
                0x00,
                0x88.toByte(),
                0x06,
                0x09,
                'l'.code.toByte(),
                'o'.code.toByte(),
                'c'.code.toByte(),
                'a'.code.toByte(),
                'l'.code.toByte(),
                'h'.code.toByte(),
                'o'.code.toByte(),
                's'.code.toByte(),
                't'.code.toByte(),
                0x63,
                0xdd.toByte(),
                0x02,
            ),
            protocolContext,
        )

        assertEquals(
            HandshakePacket(
                packetId = 0x00,
                protocolVersion = 776,
                serverAddress = "localhost",
                serverPort = 25565,
                nextState = HandshakeNextState.LOGIN,
            ),
            packet,
        )
    }

    @Test
    fun encodesHandshakePacket() {
        val packet = HandshakePacket(
            packetId = 0x00,
            protocolVersion = 776,
            serverAddress = "localhost",
            serverPort = 25565,
            nextState = HandshakeNextState.LOGIN,
        )

        assertContentEquals(
            byteArrayOf(
                0x00,
                0x88.toByte(),
                0x06,
                0x09,
                'l'.code.toByte(),
                'o'.code.toByte(),
                'c'.code.toByte(),
                'a'.code.toByte(),
                'l'.code.toByte(),
                'h'.code.toByte(),
                'o'.code.toByte(),
                's'.code.toByte(),
                't'.code.toByte(),
                0x63,
                0xdd.toByte(),
                0x02,
            ),
            codec.encode(packet, protocolContext),
        )
    }

    @Test
    fun rejectsUnsupportedNextState() {
        val buffer = byteArrayOf(
            0x00,
            0x88.toByte(),
            0x06,
            0x09,
            'l'.code.toByte(),
            'o'.code.toByte(),
            'c'.code.toByte(),
            'a'.code.toByte(),
            'l'.code.toByte(),
            'h'.code.toByte(),
            'o'.code.toByte(),
            's'.code.toByte(),
            't'.code.toByte(),
            0x63,
            0xdd.toByte(),
            0x03,
        )

        assertFailsWith<IllegalArgumentException> {
            codec.decode(buffer, protocolContext)
        }
    }

    @Test
    fun rejectsServerAddressLongerThanLimit() {
        val packet = HandshakePacket(
            packetId = 0x00,
            protocolVersion = 776,
            serverAddress = "a".repeat(JavaHandshakePacketCodec.MAX_SERVER_ADDRESS_CHARACTERS + 1),
            serverPort = 25565,
            nextState = HandshakeNextState.LOGIN,
        )

        assertFailsWith<IllegalArgumentException> {
            codec.encode(packet, protocolContext)
        }
    }

    @Test
    fun validatesHandshakePacket() {
        val accepted = codec.validate(
            HandshakePacket(
                packetId = 0x00,
                protocolVersion = 776,
                serverAddress = "localhost",
                serverPort = 25565,
                nextState = HandshakeNextState.STATUS,
            ),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = 776,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, accepted)
    }
}
