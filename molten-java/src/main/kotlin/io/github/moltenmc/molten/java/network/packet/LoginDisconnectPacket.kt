package io.github.moltenmc.molten.java.network.packet

data class LoginDisconnectPacket(
    override val packetId: Int,
    val reasonJson: String,
) : JavaPacket
