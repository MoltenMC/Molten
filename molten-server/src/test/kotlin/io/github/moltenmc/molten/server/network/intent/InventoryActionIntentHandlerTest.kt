package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.ecs.command.EcsCommandBuffer
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test

class InventoryActionIntentHandlerTest {
    @Test
    fun handlesInventoryActionIntent() {
        val handler = InventoryActionIntentHandler()
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val intent = ServerIntent.InventoryAction(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            actionId = 1,
        )
        
        handler.handle(intent, context)
        
        // Should not throw exception (implementation is TODO)
    }
}
