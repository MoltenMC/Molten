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

    @Test
    fun createsEscapedConfigurationDisconnectPacket() {
        val packet = JavaDisconnectPackets.configuration("Bad \"config\"\ntry again")

        assertEquals(0x02, packet.packetId)
        assertEquals("{\"text\":\"Bad \\\"config\\\"\\ntry again\"}", packet.reasonJson)
    }

    @Test
    fun createsEscapedPlayDisconnectPacket() {
        val packet = JavaDisconnectPackets.play("Bad \"play\"\ntry again")

        assertEquals(0x1d, packet.packetId)
        assertEquals("{\"text\":\"Bad \\\"play\\\"\\ntry again\"}", packet.reasonJson)
    }
}
