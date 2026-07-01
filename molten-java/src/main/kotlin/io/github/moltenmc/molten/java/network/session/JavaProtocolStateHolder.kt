package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.java.protocol.JavaProtocolState

class JavaProtocolStateHolder(
    initialState: JavaProtocolState = JavaProtocolState.HANDSHAKE,
) {
    @Volatile
    var state: JavaProtocolState = initialState
}
