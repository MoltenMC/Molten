package io.github.moltenmc.molten.common.spi

import io.github.moltenmc.molten.common.network.intent.ServerIntent

interface ProtocolAdapter<Packet> {
    fun toIntent(packet: Packet): ServerIntent?

    fun fromReplication(replication: Any): Packet?
}
