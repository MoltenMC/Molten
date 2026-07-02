package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
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
    fun ingressTickDrainsRegisteredSessionIntentsWithoutWritingPackets() {
        val registry = JavaSessionTickRegistry()
        val accepted = mutableListOf<ServerIntent>()
        val registered = registeredSession(registry, "First", accepted::add)
        val intent = chatIntent("hello")
        registered.sessionHolder.inboundIntentQueue.enqueue(intent)

        val drained = registry.tickIngressAll()

        assertEquals(1, drained)
        val expected: List<ServerIntent> = listOf(intent)
        assertEquals(expected, accepted)
        assertEquals(0, registered.sessionHolder.inboundIntentQueue.size)
        assertEquals(null, registered.channel.readOutbound<SystemChatPacket>())
        assertEquals(1, registered.sessionHolder.outboundQueue.size)
    }

    @Test
    fun unregisterRemovesSessionFromFutureTicks() {
        val registry = JavaSessionTickRegistry()
        val registered = registeredSession(registry, "First")

        registered.registration.unregister()
        val written = registry.tickEgressAll()

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

    private fun registeredSession(
        registry: JavaSessionTickRegistry,
        message: String,
        intentSink: (ServerIntent) -> Unit = {},
    ): RegisteredSession {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val flushHandler = JavaOutboundFlushHandler(sessionHolder)
        val tickHandler = JavaSessionTickHandler(flushHandler, sessionHolder, intentSink)
        val channel = EmbeddedChannel(flushHandler, tickHandler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent(message)))
        val registration = registry.register(tickHandler, channel.pipeline().context(tickHandler))
        return RegisteredSession(sessionHolder, channel, registration)
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(worldId = null, regionPos = null),
            message = message,
        )

    private data class RegisteredSession(
        val sessionHolder: JavaSessionHolder,
        val channel: EmbeddedChannel,
        val registration: JavaSessionTickRegistry.Registration,
    )
}
