package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class JavaSessionTickHandlerTest {
    @Test
    fun playTickFlushesQueuedOutboundMessages() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val flushHandler = JavaOutboundFlushHandler(sessionHolder)
        val tickHandler = JavaSessionTickHandler(flushHandler, sessionHolder)
        val channel = EmbeddedChannel(flushHandler, tickHandler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Tick message")))

        val written = tickHandler.tick(channel.pipeline().context(tickHandler))

        assertEquals(1, written)
        val packet = assertIs<SystemChatPacket>(channel.readOutbound())
        assertEquals("{\"text\":\"Tick message\"}", packet.contentJson)
        assertEquals(0, sessionHolder.outboundQueue.size)
    }

    @Test
    fun nonPlayTickLeavesQueueUntouched() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)
        val flushHandler = JavaOutboundFlushHandler(sessionHolder)
        val tickHandler = JavaSessionTickHandler(flushHandler, sessionHolder)
        val channel = EmbeddedChannel(flushHandler, tickHandler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Queued")))

        val written = tickHandler.tick(channel.pipeline().context(tickHandler))

        assertEquals(0, written)
        assertEquals(null, channel.readOutbound<SystemChatPacket>())
        assertEquals(1, sessionHolder.outboundQueue.size)
    }

    @Test
    fun tickDrainsInboundIntentsIntoSinkBeforeOutboundFlush() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val flushHandler = JavaOutboundFlushHandler(sessionHolder)
        val accepted = mutableListOf<ServerIntent>()
        val tickHandler = JavaSessionTickHandler(flushHandler, sessionHolder, accepted::add)
        val channel = EmbeddedChannel(flushHandler, tickHandler)
        sessionHolder.inboundIntentQueue.enqueue(chatIntent("hello"))

        val written = tickHandler.tick(channel.pipeline().context(tickHandler))

        assertEquals(0, written)
        val expected: List<ServerIntent> = listOf(chatIntent("hello"))
        assertEquals(expected, accepted)
        assertEquals(0, sessionHolder.inboundIntentQueue.size)
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(worldId = null, regionPos = null),
            message = message,
        )
}
