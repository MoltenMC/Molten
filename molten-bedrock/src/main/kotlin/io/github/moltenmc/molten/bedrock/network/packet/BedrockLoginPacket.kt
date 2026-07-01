package io.github.moltenmc.molten.bedrock.network.packet

data class BedrockLoginPacket(
    override val packetId: Int,
    val protocolVersion: Int,
    val identityChain: String,
    val clientData: String,
) : BedrockPacket
