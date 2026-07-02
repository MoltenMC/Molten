package io.github.moltenmc.molten.java.network

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.embedded.EmbeddedChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultJavaNetworkListenerTest {
    @Test
    fun bindsToLocalRandomPort() {
        val listener = DefaultJavaNetworkListener()

        try {
            listener.bind("127.0.0.1", 0)

            assertTrue(listener.isBound)
            assertNotNull(listener.localAddress)
            assertTrue(listener.localAddress!!.port > 0)
        } finally {
            listener.close()
        }
    }

    @Test
    fun rejectsDuplicateBind() {
        val listener = DefaultJavaNetworkListener()

        try {
            listener.bind("127.0.0.1", 0)

            assertFailsWith<IllegalStateException> {
                listener.bind("127.0.0.1", 0)
            }
        } finally {
            listener.close()
        }
    }

    @Test
    fun closeIsIdempotent() {
        val listener = DefaultJavaNetworkListener()

        listener.bind("127.0.0.1", 0)
        listener.close()
        listener.close()

        assertFalse(listener.isBound)
        assertEquals(null, listener.localAddress)
    }

    @Test
    fun canRebindAfterClose() {
        val listener = DefaultJavaNetworkListener()

        try {
            listener.bind("127.0.0.1", 0)
            listener.close()
            listener.bind("127.0.0.1", 0)

            assertTrue(listener.isBound)
        } finally {
            listener.close()
        }
    }

    @Test
    fun tickSessionsFlushesInitializedPlaySession() {
        val listener = DefaultJavaNetworkListener()
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val channel = EmbeddedChannel()
        listener.initializeChannel(channel, sessionHolder)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        val written = listener.tickSessions()

        assertEquals(1, written)
        assertEquals(0, sessionHolder.outboundQueue.size)
        assertNotNull(channel.readOutbound<Any>())
    }

    @Test
    fun tickIngressSessionsDrainsInboundIntentsWithoutFlushingOutboundQueue() {
        val accepted = mutableListOf<ServerIntent>()
        val listener = DefaultJavaNetworkListener(intentSink = accepted::add)
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val channel = EmbeddedChannel()
        val intent = chatIntent("hello")
        listener.initializeChannel(channel, sessionHolder)
        sessionHolder.inboundIntentQueue.enqueue(intent)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        val drained = listener.tickIngressSessions()

        assertEquals(1, drained)
        val expected: List<ServerIntent> = listOf(intent)
        assertEquals(expected, accepted)
        assertEquals(0, sessionHolder.inboundIntentQueue.size)
        assertEquals(1, sessionHolder.outboundQueue.size)
        assertEquals(null, channel.readOutbound<Any>())
    }

    @Test
    fun tickSessionsLeavesNonPlayQueue() {
        val listener = DefaultJavaNetworkListener()
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)
        val channel = EmbeddedChannel()
        listener.initializeChannel(channel, sessionHolder)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        val written = listener.tickSessions()

        assertEquals(0, written)
        assertEquals(1, sessionHolder.outboundQueue.size)
        assertEquals(null, channel.readOutbound<Any>())
    }

    @Test
    fun tickSessionsRemovesClosedInitializedChannel() {
        val listener = DefaultJavaNetworkListener()
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val channel = EmbeddedChannel()
        listener.initializeChannel(channel, sessionHolder)
        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        channel.close()

        assertEquals(0, listener.tickSessions())
        assertNotEquals(0, sessionHolder.outboundQueue.size)
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(worldId = null, regionPos = null),
            message = message,
        )
}
