package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.server.runtime.ProtocolStack

interface ProtocolListener : AutoCloseable {
    val protocol: ProtocolStack
    val isRunning: Boolean

    fun start()

    fun stop()

    override fun close() {
        stop()
    }
}
