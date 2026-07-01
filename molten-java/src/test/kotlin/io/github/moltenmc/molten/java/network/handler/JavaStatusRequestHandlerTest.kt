package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.StatusPingPacket
import io.github.moltenmc.molten.java.network.packet.StatusPongPacket
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.github.moltenmc.molten.java.status.JavaStatusResponse
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class JavaStatusRequestHandlerTest {
    @Test
    fun createsStatusResponseForRequest() {
        val handler = JavaStatusRequestHandler {
            JavaStatusResponse(
                description = "Test server",
                onlinePlayers = 2,
                maxPlayers = 50,
            )
        }

        val response = handler.responseFor(StatusRequestPacket(packetId = 0x00))

        assertEquals(0x00, response.packetId)
        assertContains(response.json, "\"text\":\"Test server\"")
        assertContains(response.json, "\"online\":2")
        assertContains(response.json, "\"max\":50")
    }

    @Test
    fun handleConvertsStatusRequestToStatusResponse() {
        val handler = JavaStatusRequestHandler {
            JavaStatusResponse(
                description = "A Molten server",
                onlinePlayers = 0,
                maxPlayers = 20,
            )
        }

        val response = handler.handle(StatusRequestPacket(packetId = 0x00))

        assertIs<StatusResponsePacket>(response)
    }

    @Test
    fun handleConvertsStatusPingToPongWithSamePayload() {
        val handler = JavaStatusRequestHandler()

        val response = handler.handle(StatusPingPacket(packetId = 0x01, payload = 123456789))

        assertEquals(StatusPongPacket(packetId = 0x01, payload = 123456789), response)
    }

    @Test
    fun nonStatusRequestPassesThrough() {
        val handler = JavaStatusRequestHandler()
        val packet = TestPacket(packetId = 0x7f)

        assertSame(packet, handler.handle(packet))
    }

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
