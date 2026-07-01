package io.github.moltenmc.molten.bedrock.network.registry

import io.github.moltenmc.molten.bedrock.network.codec.BedrockPacketCodec
import io.github.moltenmc.molten.bedrock.network.packet.BedrockPacket
import io.github.moltenmc.molten.bedrock.network.raknet.RakNetReliability
import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketFrequencyClass
import kotlin.reflect.KClass

data class BedrockPacketRegistryEntry<T : BedrockPacket>(
    val packetId: Int,
    val packetClass: KClass<T>,
    val codec: BedrockPacketCodec<T>,
    val direction: PacketDirection,
    val reliability: RakNetReliability,
    val frequencyClass: PacketFrequencyClass,
)
