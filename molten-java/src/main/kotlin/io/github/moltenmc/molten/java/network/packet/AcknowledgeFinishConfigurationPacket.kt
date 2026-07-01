package io.github.moltenmc.molten.java.network.packet

data class AcknowledgeFinishConfigurationPacket(
    override val packetId: Int,
) : JavaPacket
