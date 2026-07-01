package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.bedrock.network.BedrockNetworkListener
import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import java.util.concurrent.atomic.AtomicBoolean

class BedrockProtocolListener(
    private val configuration: ServerConfiguration,
    private val delegate: BedrockNetworkListener? = null,
) : ProtocolListener {
    private val runningRef = AtomicBoolean(false)

    override val protocol: ProtocolStack = ProtocolStack.BEDROCK_EDITION

    override val isRunning: Boolean
        get() = runningRef.get()

    override fun start() {
        if (runningRef.compareAndSet(false, true)) {
            delegate?.bind(configuration.bindAddress, configuration.bedrockPort)
        }
    }

    override fun stop() {
        if (runningRef.compareAndSet(true, false)) {
            delegate?.close()
        }
    }
}
