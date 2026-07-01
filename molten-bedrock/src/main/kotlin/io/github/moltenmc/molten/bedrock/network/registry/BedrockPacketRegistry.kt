package io.github.moltenmc.molten.bedrock.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection

class BedrockPacketRegistry {
    private val entries = HashMap<Key, BedrockPacketRegistryEntry<*>>()

    fun register(entry: BedrockPacketRegistryEntry<*>) {
        entries[Key(entry.direction, entry.packetId)] = entry
    }

    fun find(direction: PacketDirection, packetId: Int): BedrockPacketRegistryEntry<*>? =
        entries[Key(direction, packetId)]

    private data class Key(val direction: PacketDirection, val packetId: Int)
}
