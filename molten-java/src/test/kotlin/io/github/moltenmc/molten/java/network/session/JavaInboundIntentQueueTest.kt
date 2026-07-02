package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaInboundIntentQueueTest {
    @Test
    fun drainsIntentsInOrderAndClearsQueue() {
        val queue = JavaInboundIntentQueue()
        val first = chatIntent("first")
        val second = chatIntent("second")

        queue.enqueue(first)
        queue.enqueue(second)

        assertEquals(listOf(first, second), queue.drain())
        assertEquals(0, queue.size)
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(worldId = null, regionPos = null),
            message = message,
        )
}
