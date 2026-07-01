package io.github.moltenmc.molten.java.network.packet

data class PlayDisconnectPacket(
    override val packetId: Int,
    val reasonJson: String,
) : JavaPacket
