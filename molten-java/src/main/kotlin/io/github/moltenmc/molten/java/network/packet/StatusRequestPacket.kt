package io.github.moltenmc.molten.java.network.packet

data class StatusRequestPacket(
    override val packetId: Int,
) : JavaPacket
