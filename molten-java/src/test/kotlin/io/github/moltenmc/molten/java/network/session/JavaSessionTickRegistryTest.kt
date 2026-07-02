package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.handler.JavaOutboundFlushHandler
import io.github.moltenmc.molten.java.network.handler.JavaSessionTickHandler
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class JavaSessionTickRegistryTest {
    @Test
    fun ticksRegisteredSessionsAndReturnsWrittenPacketCount() {
        val registry = JavaSessionTickRegistry()
        val first = registeredSession(registry, "First")
        val second = registeredSession(registry, "Second")

        val written = registry.tickAll()

        assertEquals(2, written)
        assertEquals("{\"text\":\"First\"}", assertIs<SystemChatPacket>(first.channel.readOutbound()).contentJson)
        assertEquals("{\"text\":\"Second\"}", assertIs<SystemChatPacket>(second.channel.readOutbound()).contentJson)
    }

    @Test
    fun unregisterRemovesSessionFromFutureTicks() {
        val registry = JavaSessionTickRegistry()
        val registered = registeredSession(registry, "First")

        registered.registration.unregister()
        val written = registry.tickAll()

        assertEquals(0, written)
        assertEquals(null, registered.channel.readOutbound<SystemChatPacket>())
        assertEquals(1, registered.sessionHolder.outboundQueue.size)
        assertEquals(0, registry.size)
    }

    @Test
    fun closedChannelsAreRemovedWhenTicked() {
        val registry = JavaSessionTickRegistry()
        val registered = registeredSession(registry, "First")
        registered.channel.close()

        val written = registry.tickAll()

        assertEquals(0, written)
        assertEquals(0, registry.size)
    }

    private fun registeredSession(registry: JavaSessionTickRegistry, message: String): RegisteredSession {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val flushHandler = JavaOutboundFlushHandler(sessionHolder)
        val tickHandler = JavaSessionTickHandler(flushHandler)
        val channel = EmbeddedChannel(flushHandler, tickHandler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent(message)))
        val registration = registry.register(tickHandler, channel.pipeline().context(tickHandler))
        return RegisteredSession(sessionHolder, channel, registration)
    }

    private data class RegisteredSession(
        val sessionHolder: JavaSessionHolder,
        val channel: EmbeddedChannel,
        val registration: JavaSessionTickRegistry.Registration,
    )
}
