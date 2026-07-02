package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JavaOutboundQueueTest {
    @Test
    fun drainsMessagesInOrderAndClearsQueue() {
        val queue = JavaOutboundQueue()
        val first = OutboundMessage.System(TextComponent("First"))
        val second = OutboundMessage.CommandFeedback(TextComponent("Second"))

        queue.enqueue(first)
        queue.enqueue(second)

        assertEquals(listOf(first, second), queue.drainMessages())
        assertEquals(0, queue.size)
    }

    @Test
    fun drainsPacketsOnlyInPlayState() {
        val queue = JavaOutboundQueue()
        queue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        val packets = queue.drainPackets(JavaProtocolState.PLAY)

        val packet = assertIs<SystemChatPacket>(packets.single())
        assertEquals("{\"text\":\"Hello\"}", packet.contentJson)
        assertEquals(0, queue.size)
    }

    @Test
    fun nonPlayStateDoesNotDrainMessages() {
        val queue = JavaOutboundQueue()
        queue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        assertTrue(queue.drainPackets(JavaProtocolState.CONFIGURATION).isEmpty())
        assertEquals(1, queue.size)
    }
}
