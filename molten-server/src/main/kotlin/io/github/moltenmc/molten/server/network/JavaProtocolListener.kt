package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.java.network.JavaNetworkListener
import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import java.util.concurrent.atomic.AtomicBoolean

class JavaProtocolListener(
    private val configuration: ServerConfiguration,
    private val delegate: JavaNetworkListener? = null,
) : ProtocolListener {
    private val runningRef = AtomicBoolean(false)

    override val protocol: ProtocolStack = ProtocolStack.JAVA_EDITION

    override val isRunning: Boolean
        get() = runningRef.get()

    override fun start() {
        if (runningRef.compareAndSet(false, true)) {
            delegate?.bind(configuration.bindAddress, configuration.javaPort)
        }
    }

    override fun stop() {
        if (runningRef.compareAndSet(true, false)) {
            delegate?.close()
        }
    }
}
