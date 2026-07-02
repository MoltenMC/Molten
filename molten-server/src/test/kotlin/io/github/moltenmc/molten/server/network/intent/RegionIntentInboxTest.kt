package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class RegionIntentInboxTest {
    @Test
    fun drainsIntentsForSingleRegionInOrder() {
        val inbox = RegionIntentInbox()
        val key = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(2, 3))
        val first = chatIntent("first", key)
        val second = chatIntent("second", key)

        inbox.accept(key.worldId, key.regionPos, first)
        inbox.accept(key.worldId, key.regionPos, second)

        assertEquals(2, inbox.size(key))
        assertEquals(listOf(first, second), inbox.drain(key))
        assertEquals(0, inbox.size(key))
        assertEquals(emptyList(), inbox.drain(key))
    }

    @Test
    fun keepsRegionsIsolated() {
        val inbox = RegionIntentInbox()
        val firstKey = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(2, 3))
        val secondKey = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(4, 5))
        val first = chatIntent("first", firstKey)
        val second = chatIntent("second", secondKey)

        inbox.accept(firstKey.worldId, firstKey.regionPos, first)
        inbox.accept(secondKey.worldId, secondKey.regionPos, second)

        assertEquals(listOf(first), inbox.drain(firstKey))
        assertEquals(listOf(second), inbox.drain(secondKey))
    }

    private fun chatIntent(message: String, key: RegionIntentKey): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(key.worldId, key.regionPos),
            message = message,
        )
}
