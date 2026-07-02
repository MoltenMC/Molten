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
import io.github.moltenmc.molten.server.network.intent.RegionIntentProcessor
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionIntentSimulationTaskTest {
    @Test
    fun drainsRegionIntentBatchesDuringRegionSimulationStep() {
        val inbox = RegionIntentInbox()
        val processed = mutableListOf<RegionIntentBatch>()
        val processor = object : RegionIntentProcessor {
            override fun process(batch: RegionIntentBatch) {
                processed.add(batch)
            }
        }
        val task = RegionIntentSimulationTask(inbox, processor)
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

    @Test
    fun handlesEmptyInbox() {
        val inbox = RegionIntentInbox()
        val processed = mutableListOf<RegionIntentBatch>()
        val processor = object : RegionIntentProcessor {
            override fun process(batch: RegionIntentBatch) {
                processed.add(batch)
            }
        }
        val task = RegionIntentSimulationTask(inbox, processor)

        task.execute(currentTick = 1).get()

        assertTrue(processed.isEmpty())
    }

    @Test
    fun handlesProcessorException() {
        val inbox = RegionIntentInbox()
        val key = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(2, 3))
        val intent = chatIntent("test", key)
        inbox.accept(key.worldId, key.regionPos, intent)

        val processor = object : RegionIntentProcessor {
            override fun process(batch: RegionIntentBatch) {
                throw RuntimeException("Processor error")
            }
        }
        val task = RegionIntentSimulationTask(inbox, processor)

        val future = task.execute(currentTick = 1)
        val exception = future.handle { _, ex -> ex }.get()

        assertTrue(exception is RuntimeException)
        assertEquals("Processor error", exception.message)
    }

    @Test
    fun handlesLargeBatch() {
        val inbox = RegionIntentInbox()
        val key = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(2, 3))
        val intents = (1..1000).map { i -> chatIntent("message$i", key) }
        intents.forEach { inbox.accept(key.worldId, key.regionPos, it) }

        val processed = mutableListOf<RegionIntentBatch>()
        val processor = object : RegionIntentProcessor {
            override fun process(batch: RegionIntentBatch) {
                processed.add(batch)
            }
        }
        val task = RegionIntentSimulationTask(inbox, processor)

        task.execute(currentTick = 1).get()

        assertEquals(1, processed.size)
        assertEquals(1000, processed[0].intents.size)
        assertEquals(0, inbox.size(key))
    }

    @Test
    fun returnsProcessedIntentCount() {
        val inbox = RegionIntentInbox()
        val firstKey = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(2, 3))
        val secondKey = RegionIntentKey(WorldId(UUID(0, 1)), RegionPos(4, 5))
        inbox.accept(firstKey.worldId, firstKey.regionPos, chatIntent("first", firstKey))
        inbox.accept(secondKey.worldId, secondKey.regionPos, chatIntent("second", secondKey))
        inbox.accept(firstKey.worldId, firstKey.regionPos, chatIntent("third", firstKey))

        val task = RegionIntentSimulationTask(inbox)

        val count = task.drainAndProcess()

        assertEquals(3, count)
    }

    private fun chatIntent(message: String, key: RegionIntentKey): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(key.worldId, key.regionPos),
            message = message,
        )
}
