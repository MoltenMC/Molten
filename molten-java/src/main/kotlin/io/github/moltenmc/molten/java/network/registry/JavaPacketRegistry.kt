package io.github.moltenmc.molten.java.network.registry

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.java.protocol.JavaProtocolState

class JavaPacketRegistry {
    private val entries = HashMap<Key, JavaPacketRegistryEntry<*>>()

    fun register(entry: JavaPacketRegistryEntry<*>) {
        entries[Key(entry.state, entry.direction, entry.packetId)] = entry
    }

    fun find(state: JavaProtocolState, direction: PacketDirection, packetId: Int): JavaPacketRegistryEntry<*>? =
        entries[Key(state, direction, packetId)]

    private data class Key(
        val state: JavaProtocolState,
        val direction: PacketDirection,
        val packetId: Int,
    )
}
