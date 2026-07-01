package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.FinishConfigurationPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class JavaConfigurationStartHandlerTest {
    @Test
    fun createsFinishConfigurationPacketInConfigurationState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)
        val packets = JavaConfigurationStartHandler().configurationPacketsFor(sessionHolder)

        assertEquals(1, packets.size)
        assertEquals(0x03, assertIs<FinishConfigurationPacket>(packets.single()).packetId)
    }

    @Test
    fun rejectsNonConfigurationState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)

        assertFailsWith<IllegalArgumentException> {
            JavaConfigurationStartHandler().configurationPacketsFor(sessionHolder)
        }
    }
}
