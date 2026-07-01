package io.github.moltenmc.molten.java.network.packet

enum class HandshakeNextState(val wireValue: Int) {
    STATUS(1),
    LOGIN(2),
    ;

    companion object {
        fun fromWireValue(value: Int): HandshakeNextState =
            entries.firstOrNull { it.wireValue == value }
                ?: throw IllegalArgumentException("Unsupported handshake next state: $value.")
    }
}
