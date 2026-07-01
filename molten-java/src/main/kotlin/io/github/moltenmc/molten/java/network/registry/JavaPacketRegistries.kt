package io.github.moltenmc.molten.java.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketFrequencyClass
import io.github.moltenmc.molten.java.network.codec.JavaHandshakePacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusRequestPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusResponsePacketCodec
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState

object JavaPacketRegistries {
    fun protocol776(): JavaPacketRegistry =
        JavaPacketRegistry().apply {
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaHandshakePacketCodec.PACKET_ID,
                    packetClass = HandshakePacket::class,
                    codec = JavaHandshakePacketCodec(),
                    state = JavaProtocolState.HANDSHAKE,
                    direction = PacketDirection.SERVERBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaStatusRequestPacketCodec.PACKET_ID,
                    packetClass = StatusRequestPacket::class,
                    codec = JavaStatusRequestPacketCodec(),
                    state = JavaProtocolState.STATUS,
                    direction = PacketDirection.SERVERBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaStatusResponsePacketCodec.PACKET_ID,
                    packetClass = StatusResponsePacket::class,
                    codec = JavaStatusResponsePacketCodec(),
                    state = JavaProtocolState.STATUS,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
        }
}
