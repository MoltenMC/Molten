package io.github.moltenmc.molten.java.network.packet

data class StatusPongPacket(
    override val packetId: Int,
    val payload: Long,
) : JavaPacket
