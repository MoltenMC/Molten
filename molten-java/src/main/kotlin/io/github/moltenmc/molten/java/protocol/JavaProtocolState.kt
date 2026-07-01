package io.github.moltenmc.molten.java.protocol

enum class JavaProtocolState {
    HANDSHAKE,
    STATUS,
    LOGIN,
    CONFIGURATION,
    PLAY,
    DISCONNECTED,
}
