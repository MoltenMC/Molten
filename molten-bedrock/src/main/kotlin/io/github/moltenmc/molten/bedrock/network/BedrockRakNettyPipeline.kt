package io.github.moltenmc.molten.bedrock.network

enum class BedrockRakNettyLayer(val order: Int) {
    UDP_SOCKET_TRANSPORT(1),
    OFFLINE_PING_PONG(2),
    CONNECTION_NEGOTIATION(3),
    RELIABILITY(4),
    ORDERING_CHANNEL(5),
    FRAGMENTATION_REASSEMBLY(6),
    BATCH_DECODER(7),
    COMPRESSION_DECODER(8),
    ENCRYPTION(9),
    PACKET_DECODER(10),
    SESSION_STATE(11),
    PACKET_HANDLER(12),
}
