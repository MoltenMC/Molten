package io.github.moltenmc.molten.java.network

enum class JavaNettyInboundHandler(val order: Int) {
    READ_TIMEOUT(1),
    LEGACY_PING(2),
    VAR_INT_FRAME_DECODER(3),
    PACKET_DECRYPTION(4),
    PACKET_COMPRESSION_DECODER(5),
    PACKET_DECODER(6),
    SESSION_STATE(7),
    PACKET_HANDLER(8),
}

enum class JavaNettyOutboundHandler(val order: Int) {
    PACKET_ENCODER(1),
    PACKET_COMPRESSION_ENCODER(2),
    PACKET_ENCRYPTION(3),
    VAR_INT_FRAME_ENCODER(4),
}
