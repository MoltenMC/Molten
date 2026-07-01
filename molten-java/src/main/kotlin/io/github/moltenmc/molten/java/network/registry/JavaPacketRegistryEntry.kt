package io.github.moltenmc.molten.java.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketFrequencyClass
import io.github.moltenmc.molten.java.network.codec.JavaPacketCodec
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.reflect.KClass

data class JavaPacketRegistryEntry<T : JavaPacket>(
    val packetId: Int,
    val packetClass: KClass<T>,
    val codec: JavaPacketCodec<T>,
    val state: JavaProtocolState,
    val direction: PacketDirection,
    val frequencyClass: PacketFrequencyClass,
)
