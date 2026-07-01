package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.text.TextComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavaChatPacketsTest {
    @Test
    fun createsSystemChatPacket() {
        val packet = JavaChatPackets.system(TextComponent("Hello \"chat\"\nline"))

        assertEquals(0x72, packet.packetId)
        assertEquals("{\"text\":\"Hello \\\"chat\\\"\\nline\"}", packet.contentJson)
        assertFalse(packet.overlay)
    }

    @Test
    fun createsOverlaySystemChatPacket() {
        val packet = JavaChatPackets.system(TextComponent("Action bar"), overlay = true)

        assertEquals("{\"text\":\"Action bar\"}", packet.contentJson)
        assertTrue(packet.overlay)
    }
}
