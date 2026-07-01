package io.github.moltenmc.molten.bedrock.network

import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicBoolean

class DefaultBedrockNetworkListener : BedrockNetworkListener {
    private val boundRef = AtomicBoolean(false)

    @Volatile
    private var channel: DatagramChannel? = null

    val isBound: Boolean
        get() = boundRef.get()

    val localAddress: InetSocketAddress?
        get() = channel?.localAddress as? InetSocketAddress

    override fun bind(host: String, port: Int) {
        require(host.isNotBlank()) { "Bind host is required." }
        require(port in PORT_RANGE) { "Bind port is out of range." }
        check(boundRef.compareAndSet(false, true)) { "Bedrock network listener is already bound." }

        try {
            channel = DatagramChannel.open()
                .bind(InetSocketAddress(host, port))
        } catch (error: Throwable) {
            boundRef.set(false)
            channel?.close()
            channel = null
            throw IllegalStateException("Failed to bind Bedrock network listener to $host:$port.", error)
        }
    }

    override fun close() {
        if (!boundRef.compareAndSet(true, false)) {
            return
        }

        val current = channel
        channel = null
        current?.close()
    }

    companion object {
        private val PORT_RANGE = 0..65535
    }
}
