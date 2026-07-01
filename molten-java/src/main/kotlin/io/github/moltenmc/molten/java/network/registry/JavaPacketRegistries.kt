package io.github.moltenmc.molten.java.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketFrequencyClass
import io.github.moltenmc.molten.java.network.codec.JavaAcknowledgeFinishConfigurationPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaConfigurationDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaFinishConfigurationPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaHandshakePacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaLoginDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaLoginStartPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaLoginSuccessPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaPlayDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaPlayJoinPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusPingPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusPongPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusRequestPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaStatusResponsePacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaSystemChatPacketCodec
import io.github.moltenmc.molten.java.network.packet.AcknowledgeFinishConfigurationPacket
import io.github.moltenmc.molten.java.network.packet.ConfigurationDisconnectPacket
import io.github.moltenmc.molten.java.network.packet.FinishConfigurationPacket
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import io.github.moltenmc.molten.java.network.packet.JavaPlayJoinPacket
import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import io.github.moltenmc.molten.java.network.packet.PlayDisconnectPacket
import io.github.moltenmc.molten.java.network.packet.StatusPingPacket
import io.github.moltenmc.molten.java.network.packet.StatusPongPacket
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
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
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaStatusPingPacketCodec.PACKET_ID,
                    packetClass = StatusPingPacket::class,
                    codec = JavaStatusPingPacketCodec(),
                    state = JavaProtocolState.STATUS,
                    direction = PacketDirection.SERVERBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaStatusPongPacketCodec.PACKET_ID,
                    packetClass = StatusPongPacket::class,
                    codec = JavaStatusPongPacketCodec(),
                    state = JavaProtocolState.STATUS,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaLoginStartPacketCodec.PACKET_ID,
                    packetClass = LoginStartPacket::class,
                    codec = JavaLoginStartPacketCodec(),
                    state = JavaProtocolState.LOGIN,
                    direction = PacketDirection.SERVERBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaLoginSuccessPacketCodec.PACKET_ID,
                    packetClass = LoginSuccessPacket::class,
                    codec = JavaLoginSuccessPacketCodec(),
                    state = JavaProtocolState.LOGIN,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaLoginDisconnectPacketCodec.PACKET_ID,
                    packetClass = LoginDisconnectPacket::class,
                    codec = JavaLoginDisconnectPacketCodec(),
                    state = JavaProtocolState.LOGIN,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaFinishConfigurationPacketCodec.PACKET_ID,
                    packetClass = FinishConfigurationPacket::class,
                    codec = JavaFinishConfigurationPacketCodec(),
                    state = JavaProtocolState.CONFIGURATION,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaConfigurationDisconnectPacketCodec.PACKET_ID,
                    packetClass = ConfigurationDisconnectPacket::class,
                    codec = JavaConfigurationDisconnectPacketCodec(),
                    state = JavaProtocolState.CONFIGURATION,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaAcknowledgeFinishConfigurationPacketCodec.PACKET_ID,
                    packetClass = AcknowledgeFinishConfigurationPacket::class,
                    codec = JavaAcknowledgeFinishConfigurationPacketCodec(),
                    state = JavaProtocolState.CONFIGURATION,
                    direction = PacketDirection.SERVERBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaPlayDisconnectPacketCodec.PACKET_ID,
                    packetClass = PlayDisconnectPacket::class,
                    codec = JavaPlayDisconnectPacketCodec(),
                    state = JavaProtocolState.PLAY,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaPlayJoinPacketCodec.PACKET_ID,
                    packetClass = JavaPlayJoinPacket::class,
                    codec = JavaPlayJoinPacketCodec(),
                    state = JavaProtocolState.PLAY,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
            register(
                JavaPacketRegistryEntry(
                    packetId = JavaSystemChatPacketCodec.PACKET_ID,
                    packetClass = SystemChatPacket::class,
                    codec = JavaSystemChatPacketCodec(),
                    state = JavaProtocolState.PLAY,
                    direction = PacketDirection.CLIENTBOUND,
                    frequencyClass = PacketFrequencyClass.LOW,
                ),
            )
        }
}
