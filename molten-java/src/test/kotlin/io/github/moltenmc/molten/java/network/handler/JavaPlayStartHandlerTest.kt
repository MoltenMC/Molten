package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.JavaPlayJoinPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class JavaPlayStartHandlerTest {
    @Test
    fun createsPlayJoinPacketInPlayState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val packets = JavaPlayStartHandler().playPacketsFor(sessionHolder)

        assertEquals(1, packets.size)
        val packet = assertIs<JavaPlayJoinPacket>(packets.single())
        assertEquals(0x2b, packet.packetId)
        assertEquals("minecraft:overworld", packet.worldName)
    }

    @Test
    fun rejectsNonPlayState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.CONFIGURATION)

        assertFailsWith<IllegalArgumentException> {
            JavaPlayStartHandler().playPacketsFor(sessionHolder)
        }
    }
}
