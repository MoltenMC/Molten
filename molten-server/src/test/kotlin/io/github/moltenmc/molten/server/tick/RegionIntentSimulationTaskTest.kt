package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import io.github.moltenmc.molten.server.network.intent.RegionIntentBatch
import io.github.moltenmc.molten.server.network.intent.RegionIntentInbox
import io.github.moltenmc.molten.server.network.intent.RegionIntentKey
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class RegionIntentSimulationTaskTest {
    @Test
    fun drainsRegionIntentBatchesDuringRegionSimulationStep() {
        val inbox = RegionIntentInbox()
        val processed = mutableListOf<RegionIntentBatch>()
        val task = RegionIntentSimulationTask(inbox, processed::add)
        val firstKey = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(2, 3))
        val secondKey = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(4, 5))
        val first = chatIntent("first", firstKey)
        val second = chatIntent("second", secondKey)
        inbox.accept(firstKey.worldId, firstKey.regionPos, first)
        inbox.accept(secondKey.worldId, secondKey.regionPos, second)

        task.execute(currentTick = 3).get()

        assertEquals(TickPipelineStep.REGION_SIMULATION, task.step)
        assertEquals(
            setOf(
                RegionIntentBatch(firstKey, listOf(first)),
                RegionIntentBatch(secondKey, listOf(second)),
            ),
            processed.toSet(),
        )
        assertEquals(0, inbox.size(firstKey))
        assertEquals(0, inbox.size(secondKey))
    }

    private fun chatIntent(message: String, key: RegionIntentKey): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(key.worldId, key.regionPos),
            message = message,
        )
}
