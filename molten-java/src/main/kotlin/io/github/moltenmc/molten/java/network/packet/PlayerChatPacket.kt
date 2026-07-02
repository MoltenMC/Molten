package io.github.moltenmc.molten.java.network.packet

data class PlayerChatPacket(
    override val packetId: Int,
    val message: String,
    val timestamp: Long,
    val salt: Long,
) : JavaPacket
