package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerIntentInboxTest {
    @Test
    fun drainsAcceptedIntentsInOrderAndClearsInbox() {
        val inbox = ServerIntentInbox()
        val first = chatIntent("first")
        val second = chatIntent("second")

        inbox.accept(first)
        inbox.accept(second)

        assertEquals(listOf(first, second), inbox.drain())
        assertEquals(0, inbox.size)
        assertEquals(emptyList(), inbox.drain())
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(worldId = null, regionPos = null),
            message = message,
        )
}
