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

class PlayerChatIntentHandlerTest {
    @Test
    fun handlesChatIntentWithoutEventPublisher() {
        val handler = PlayerChatIntentHandler()
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val intent = chatIntent("Hello, world!")
        
        handler.handle(intent, context)
        
        // Should not throw exception even without event publisher
    }

    @Test
    fun publishesChatEventWithPublisher() {
        val publishedEvents = mutableListOf<ChatEvent>()
        val publisher = object : ChatEventPublisher {
            override fun publishChatEvent(event: ChatEvent) {
                publishedEvents.add(event)
            }
        }
        
        val handler = PlayerChatIntentHandler(publisher)
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val intent = chatIntent("Test message")
        
        handler.handle(intent, context)
        
        assertEquals(1, publishedEvents.size)
        assertEquals("Test message", publishedEvents[0].message)
        assertEquals(intent.sourceEntityId, publishedEvents[0].sourceEntityId)
    }

    @Test
    fun noopPublisherDoesNothing() {
        val publisher = NoopChatEventPublisher()
        val handler = PlayerChatIntentHandler(publisher)
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val intent = chatIntent("Test")
        
        handler.handle(intent, context)
        
        // Should not throw exception
    }

    @Test
    fun handlesEmptyMessage() {
        val publishedEvents = mutableListOf<ChatEvent>()
        val publisher = object : ChatEventPublisher {
            override fun publishChatEvent(event: ChatEvent) {
                publishedEvents.add(event)
            }
        }
        
        val handler = PlayerChatIntentHandler(publisher)
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val intent = chatIntent("")
        
        handler.handle(intent, context)
        
        // TODO: Add message validation when implemented
        assertEquals(1, publishedEvents.size)
    }

    @Test
    fun handlesLongMessage() {
        val publishedEvents = mutableListOf<ChatEvent>()
        val publisher = object : ChatEventPublisher {
            override fun publishChatEvent(event: ChatEvent) {
                publishedEvents.add(event)
            }
        }
        
        val handler = PlayerChatIntentHandler(publisher)
        val commandBuffer = EcsCommandBuffer()
        val context = IntentHandlerContext(commandBuffer)
        
        val longMessage = "a".repeat(1000)
        val intent = chatIntent(longMessage)
        
        handler.handle(intent, context)
        
        // TODO: Add message length validation when implemented
        assertEquals(1, publishedEvents.size)
    }

    private fun chatIntent(message: String): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            message = message,
        )
}
