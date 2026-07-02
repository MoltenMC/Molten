package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class JavaOutboundFlushHandlerTest {
    @Test
    fun playStateWritesQueuedMessagesAndClearsQueue() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val handler = JavaOutboundFlushHandler(sessionHolder)
        val channel = EmbeddedChannel(handler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        val written = handler.flushQueuedMessages(channel.pipeline().context(handler))

        assertEquals(1, written)
        val packet = assertIs<SystemChatPacket>(channel.readOutbound())
        assertEquals("{\"text\":\"Hello\"}", packet.contentJson)
        assertEquals(0, sessionHolder.outboundQueue.size)
    }

    @Test
    fun nonPlayStateDoesNotWriteOrDrainQueue() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)
        val handler = JavaOutboundFlushHandler(sessionHolder)
        val channel = EmbeddedChannel(handler)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        val written = handler.flushQueuedMessages(channel.pipeline().context(handler))

        assertEquals(0, written)
        assertEquals(null, channel.readOutbound<SystemChatPacket>())
        assertEquals(1, sessionHolder.outboundQueue.size)
    }
}
