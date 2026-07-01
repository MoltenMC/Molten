package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.bedrock.network.BedrockNetworkListener
import io.github.moltenmc.molten.java.network.JavaNetworkListener
import io.github.moltenmc.molten.server.ServerConfiguration
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProtocolListenerTest {
    @Test
    fun javaListenerBindsConfiguredAddressAndPort() {
        val delegate = RecordingJavaNetworkListener()
        val configuration = ServerConfiguration.defaults().copy(
            bindAddress = "127.0.0.1",
            javaPort = 25566,
        )
        val listener = JavaProtocolListener(configuration, delegate)

        listener.start()

        assertTrue(listener.isRunning)
        assertEquals(listOf("127.0.0.1" to 25566), delegate.binds)
        assertEquals("127.0.0.1:25566", listener.boundAddress)

        listener.stop()

        assertFalse(listener.isRunning)
        assertEquals(1, delegate.closeCalls)
    }

    @Test
    fun bedrockListenerBindsConfiguredAddressAndPort() {
        val delegate = RecordingBedrockNetworkListener()
        val configuration = ServerConfiguration.defaults().copy(
            bindAddress = "127.0.0.1",
            bedrockPort = 19133,
        )
        val listener = BedrockProtocolListener(configuration, delegate)

        listener.start()

        assertTrue(listener.isRunning)
        assertEquals(listOf("127.0.0.1" to 19133), delegate.binds)
        assertEquals("127.0.0.1:19133", listener.boundAddress)

        listener.stop()

        assertFalse(listener.isRunning)
        assertEquals(1, delegate.closeCalls)
    }

    @Test
    fun listenerStartAndStopAreIdempotent() {
        val delegate = RecordingJavaNetworkListener()
        val listener = JavaProtocolListener(ServerConfiguration.defaults(), delegate)

        listener.start()
        listener.start()
        listener.stop()
        listener.stop()

        assertEquals(1, delegate.binds.size)
        assertEquals(1, delegate.closeCalls)
    }

    private class RecordingJavaNetworkListener : JavaNetworkListener {
        val binds = mutableListOf<Pair<String, Int>>()
        var closeCalls: Int = 0

        override var localAddress: InetSocketAddress? = null
            private set

        override fun bind(host: String, port: Int) {
            binds += host to port
            localAddress = InetSocketAddress(host, port)
        }

        override fun close() {
            closeCalls++
            localAddress = null
        }
    }

    private class RecordingBedrockNetworkListener : BedrockNetworkListener {
        val binds = mutableListOf<Pair<String, Int>>()
        var closeCalls: Int = 0

        override var localAddress: InetSocketAddress? = null
            private set

        override fun bind(host: String, port: Int) {
            binds += host to port
            localAddress = InetSocketAddress(host, port)
        }

        override fun close() {
            closeCalls++
            localAddress = null
        }
    }
}
