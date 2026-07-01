package io.github.moltenmc.molten.java.network.packet

data class StatusResponsePacket(
    override val packetId: Int,
    val json: String,
) : JavaPacket
