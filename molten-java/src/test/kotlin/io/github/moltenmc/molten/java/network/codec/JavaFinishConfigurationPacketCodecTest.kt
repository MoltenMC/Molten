package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.FinishConfigurationPacket
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaFinishConfigurationPacketCodecTest {
    private val codec = JavaFinishConfigurationPacketCodec()
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.CLIENTBOUND,
    )

    @Test
    fun decodesFinishConfigurationPacket() {
        val packet = codec.decode(byteArrayOf(0x03), protocolContext)

        assertEquals(FinishConfigurationPacket(packetId = 0x03), packet)
    }

    @Test
    fun encodesFinishConfigurationPacket() {
        val packet = FinishConfigurationPacket(packetId = 0x03)

        assertContentEquals(byteArrayOf(0x03), codec.encode(packet, protocolContext))
    }

    @Test
    fun rejectsTrailingBytes() {
        assertFailsWith<IllegalArgumentException> {
            codec.decode(byteArrayOf(0x03, 0x00), protocolContext)
        }
    }

    @Test
    fun validatesFinishConfigurationPacket() {
        val result = codec.validate(
            FinishConfigurationPacket(packetId = 0x03),
            SessionContext(
                connectionId = UUID(0, 1),
                remoteAddress = null,
                protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            ),
        )

        assertEquals(PacketValidationResult.Accepted, result)
    }
}
