package io.github.moltenmc.molten.java.network.message

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JavaOutboundMessageAdapterTest {
    private val adapter = JavaOutboundMessageAdapter()

    @Test
    fun convertsSystemMessageToSystemChatPacket() {
        val packets = adapter.toPackets(
            OutboundMessage.System(TextComponent("Hello \"world\"\nline")),
        )

        val packet = assertIs<SystemChatPacket>(packets.single())
        assertEquals(0x72, packet.packetId)
        assertEquals("{\"text\":\"Hello \\\"world\\\"\\nline\"}", packet.contentJson)
        assertFalse(packet.overlay)
    }

    @Test
    fun convertsCommandFeedbackToSystemChatPacket() {
        val packets = adapter.toPackets(
            OutboundMessage.CommandFeedback(TextComponent("Saved"), overlay = true),
        )

        val packet = assertIs<SystemChatPacket>(packets.single())
        assertEquals("{\"text\":\"Saved\"}", packet.contentJson)
        assertTrue(packet.overlay)
    }
}
