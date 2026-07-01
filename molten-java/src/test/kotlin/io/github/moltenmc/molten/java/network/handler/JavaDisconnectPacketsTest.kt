package io.github.moltenmc.molten.java.network.handler

import kotlin.test.Test
import kotlin.test.assertEquals

class JavaDisconnectPacketsTest {
    @Test
    fun createsEscapedLoginDisconnectPacket() {
        val packet = JavaDisconnectPackets.login("Bad \"name\"\ntry again")

        assertEquals(0x00, packet.packetId)
        assertEquals("{\"text\":\"Bad \\\"name\\\"\\ntry again\"}", packet.reasonJson)
    }
}
