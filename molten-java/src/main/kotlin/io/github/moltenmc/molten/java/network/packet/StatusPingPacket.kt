package io.github.moltenmc.molten.java.network.packet

data class StatusPingPacket(
    override val packetId: Int,
    val payload: Long,
) : JavaPacket
