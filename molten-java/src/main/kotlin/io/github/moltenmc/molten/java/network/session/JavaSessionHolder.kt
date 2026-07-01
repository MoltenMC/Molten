package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.java.protocol.JavaProtocolState

class JavaSessionHolder(
    initialState: JavaProtocolState = JavaProtocolState.HANDSHAKE,
) : JavaProtocolStateHolder(initialState) {
    @Volatile
    var profile: JavaSessionProfile? = null
}
