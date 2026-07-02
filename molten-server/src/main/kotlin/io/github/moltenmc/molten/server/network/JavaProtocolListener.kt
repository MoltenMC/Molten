package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.java.network.JavaNetworkListener
import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

class JavaProtocolListener(
    private val configuration: ServerConfiguration,
    private val delegate: JavaNetworkListener? = null,
) : ProtocolListener {
    private val runningRef = AtomicBoolean(false)

    override val protocol: ProtocolStack = ProtocolStack.JAVA_EDITION

    override val isRunning: Boolean
        get() = runningRef.get()

    override val boundAddress: String?
        get() = delegate?.localAddress?.formatEndpoint()
            ?: "${configuration.bindAddress}:${configuration.javaPort}".takeIf { isRunning }

    override fun start() {
        if (runningRef.compareAndSet(false, true)) {
            try {
                delegate?.bind(configuration.bindAddress, configuration.javaPort)
            } catch (error: Throwable) {
                runningRef.set(false)
                throw error
            }
        }
    }

    override fun stop() {
        if (runningRef.compareAndSet(true, false)) {
            delegate?.close()
        }
    }

    override fun tick(): Int =
        if (isRunning) {
            delegate?.tickSessions() ?: 0
        } else {
            0
        }

    private fun InetSocketAddress.formatEndpoint(): String =
        "${hostString}:${port}"
}
