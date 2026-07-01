package io.github.moltenmc.molten.java.network.packet

data class SystemChatPacket(
    override val packetId: Int,
    val contentJson: String,
    val overlay: Boolean,
) : JavaPacket
