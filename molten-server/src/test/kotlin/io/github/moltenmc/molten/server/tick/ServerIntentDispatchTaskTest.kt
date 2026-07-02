package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.server.network.ServerIntentInbox
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerIntentDispatchTaskTest {
    @Test
    fun dispatchesInboxIntentsDuringIntentRoutingStep() {
        val inbox = ServerIntentInbox()
        val accepted = mutableListOf<ServerIntent>()
        val task = ServerIntentDispatchTask(inbox, accepted::add)
        val intent = chatIntent("hello")
        inbox.accept(intent)

        task.execute(currentTick = 3).get()

        assertEquals(TickPipelineStep.INTENT_ROUTING, task.step)
        val expected: List<ServerIntent> = listOf(intent)
        assertEquals(expected, accepted)
        assertEquals(0, inbox.size)
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(worldId = null, regionPos = null),
            message = message,
        )
}
