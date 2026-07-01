package io.github.moltenmc.molten.java.network.packet

data class FinishConfigurationPacket(
    override val packetId: Int,
) : JavaPacket
