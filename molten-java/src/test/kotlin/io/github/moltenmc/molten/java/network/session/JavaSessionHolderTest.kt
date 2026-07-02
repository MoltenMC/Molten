package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaSessionHolderTest {
    @Test
    fun ownsOutboundQueue() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)

        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        assertEquals(1, sessionHolder.outboundQueue.size)
    }
}
