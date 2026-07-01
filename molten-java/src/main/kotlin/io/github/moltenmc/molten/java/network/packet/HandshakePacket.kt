package io.github.moltenmc.molten.java.network.packet

data class HandshakePacket(
    override val packetId: Int,
    val protocolVersion: Int,
    val serverAddress: String,
    val serverPort: Int,
    val nextState: HandshakeNextState,
) : JavaPacket
