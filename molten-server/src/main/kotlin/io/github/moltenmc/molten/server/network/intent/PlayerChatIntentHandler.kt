package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.StructuralChange
import io.github.moltenmc.molten.common.network.intent.ServerIntent

/**
 * Handles PlayerChat intents by processing chat messages and broadcasting to other players.
 */
class PlayerChatIntentHandler(
    private val chatEventPublisher: ChatEventPublisher? = null,
) : IntentHandler<ServerIntent.PlayerChat> {
    override fun handle(intent: ServerIntent.PlayerChat, context: IntentHandlerContext) {
        // TODO: Validate chat message (length, content filtering, rate limiting)
        
        // Publish chat event if event publisher is available
        chatEventPublisher?.publishChatEvent(
            ChatEvent(
                sourceEntityId = intent.sourceEntityId,
                message = intent.message,
                routing = intent.routing,
            ),
        )
        
        // TODO: Broadcast chat message to other players in the same region/world
        // This will require:
        // - Query for nearby players
        // - Send chat packets to their sessions
        // - Handle cross-region broadcasting
    }
}

/**
 * Event representing a player chat message.
 */
data class ChatEvent(
    val sourceEntityId: EntityId,
    val message: String,
    val routing: io.github.moltenmc.molten.common.network.IntentRouting,
)

/**
 * Interface for publishing chat events to the event bus.
 */
interface ChatEventPublisher {
    fun publishChatEvent(event: ChatEvent)
}

/**
 * No-op implementation of ChatEventPublisher for testing and scaffolding.
 */
class NoopChatEventPublisher : ChatEventPublisher {
    override fun publishChatEvent(event: ChatEvent) {
        // No-op
    }
}
