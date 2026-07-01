package io.github.moltenmc.molten.java.network.packet

data class ConfigurationDisconnectPacket(
    override val packetId: Int,
    val reasonJson: String,
) : JavaPacket
