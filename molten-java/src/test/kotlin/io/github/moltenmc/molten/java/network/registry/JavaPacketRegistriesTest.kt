package io.github.moltenmc.molten.java.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.java.network.codec.JavaHandshakePacketCodec
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JavaPacketRegistriesTest {
    @Test
    fun protocol776RegistersServerboundHandshake() {
        val entry = JavaPacketRegistries.protocol776().find(
            state = JavaProtocolState.HANDSHAKE,
            direction = PacketDirection.SERVERBOUND,
            packetId = JavaHandshakePacketCodec.PACKET_ID,
        )

        assertNotNull(entry)
        assertEquals(HandshakePacket::class, entry.packetClass)
    }
}
