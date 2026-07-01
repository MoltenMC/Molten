package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JavaLoginStartHandlerTest {
    @Test
    fun loginStartStoresProfileAndMovesToConfigurationState() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)
        val handler = JavaLoginStartHandler(sessionHolder)
        val uuid = UUID.fromString("12345678-1234-5678-9abc-def012345678")
        val packet = LoginStartPacket(
            packetId = 0x00,
            name = "Player",
            playerUuid = uuid,
        )

        assertEquals(
            LoginSuccessPacket(
                packetId = 0x02,
                uuid = uuid,
                username = "Player",
            ),
            handler.handle(packet),
        )
        assertEquals("Player", sessionHolder.profile?.name)
        assertEquals(uuid, sessionHolder.profile?.uuid)
        assertEquals(JavaProtocolState.CONFIGURATION, sessionHolder.state)
    }

    @Test
    fun nonLoginPacketPassesThroughWithoutChangingSession() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.LOGIN)
        val handler = JavaLoginStartHandler(sessionHolder)
        val packet = TestPacket(packetId = 0x7f)

        assertSame(packet, handler.handle(packet))
        assertEquals(null, sessionHolder.profile)
        assertEquals(JavaProtocolState.LOGIN, sessionHolder.state)
    }

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket {
        @Suppress("unused")
        val protocolVersion: Int = JavaEditionProtocol.PROTOCOL_VERSION
    }
}
