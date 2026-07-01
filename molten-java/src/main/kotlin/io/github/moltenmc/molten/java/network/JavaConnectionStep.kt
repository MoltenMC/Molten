package io.github.moltenmc.molten.java.network

enum class JavaConnectionStep(val order: Int) {
    HANDSHAKE(1),
    STATUS(2),
    LOGIN_START(3),
    ENCRYPTION(4),
    COMPRESSION(5),
    CONFIGURATION(6),
    PLAY(7),
}
