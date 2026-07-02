package io.github.moltenmc.molten.common.network.message

import io.github.moltenmc.molten.common.text.TextComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OutboundMessageTest {
    @Test
    fun createsSystemMessage() {
        val message = OutboundMessage.System(TextComponent("Hello"))

        assertEquals("Hello", message.content.plainText)
        assertFalse(message.overlay)
    }

    @Test
    fun createsOverlayCommandFeedback() {
        val message = OutboundMessage.CommandFeedback(TextComponent("Saved"), overlay = true)

        assertEquals("Saved", message.content.plainText)
        assertTrue(message.overlay)
    }
}
