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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntentHandlerRegistryTest {
    @Test
    fun registersAndRetrievesHandler() {
        val registry = IntentHandlerRegistry()
        val handler = TestIntentHandler()
        
        registry.register(ServerIntent.PlayerChat::class.java, handler)
        
        val retrieved = registry.getHandler(ServerIntent.PlayerChat::class.java)
        assertEquals(handler, retrieved)
    }

    @Test
    fun handlesIntentWithRegisteredHandler() {
        val registry = IntentHandlerRegistry()
        val handler = TestIntentHandler()
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        registry.register(ServerIntent.PlayerChat::class.java, handler)
        
        val intent = ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            message = "test",
        )
        
        val handled = registry.handle(intent, context)
        
        assertTrue(handled)
        assertTrue(handler.wasCalled)
    }

    @Test
    fun returnsFalseForUnregisteredIntent() {
        val registry = IntentHandlerRegistry()
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val intent = ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            message = "test",
        )
        
        val handled = registry.handle(intent, context)
        
        assertFalse(handled)
    }

    @Test
    fun handlesMultipleIntentTypes() {
        val registry = IntentHandlerRegistry()
        val chatHandler = TestIntentHandler()
        val moveHandler = TestMoveIntentHandler()
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        registry.register(ServerIntent.PlayerChat::class.java, chatHandler)
        registry.register(ServerIntent.PlayerMove::class.java, moveHandler)
        
        val chatIntent = ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            message = "test",
        )
        
        val moveIntent = ServerIntent.PlayerMove(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            x = 0.0,
            y = 0.0,
            z = 0.0,
            yaw = 0f,
            pitch = 0f,
        )
        
        registry.handle(chatIntent, context)
        registry.handle(moveIntent, context)
        
        assertTrue(chatHandler.wasCalled)
        assertTrue(moveHandler.wasCalled)
    }

    class TestIntentHandler : IntentHandler<ServerIntent.PlayerChat> {
        var wasCalled = false
        
        override fun handle(intent: ServerIntent.PlayerChat, context: IntentHandlerContext) {
            wasCalled = true
        }
    }

    class TestMoveIntentHandler : IntentHandler<ServerIntent.PlayerMove> {
        var wasCalled = false
        
        override fun handle(intent: ServerIntent.PlayerMove, context: IntentHandlerContext) {
            wasCalled = true
        }
    }
}
