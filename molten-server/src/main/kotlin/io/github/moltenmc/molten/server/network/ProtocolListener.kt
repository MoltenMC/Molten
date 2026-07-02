package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.server.runtime.ProtocolStack

interface ProtocolListener : AutoCloseable {
    val protocol: ProtocolStack
    val isRunning: Boolean
    val boundAddress: String?
        get() = null

    fun start()

    fun tickIngress(): Int = 0

    fun tickEgress(): Int = 0

    fun tick(): Int = tickEgress()

    fun stop()

    override fun close() {
        stop()
    }
}
