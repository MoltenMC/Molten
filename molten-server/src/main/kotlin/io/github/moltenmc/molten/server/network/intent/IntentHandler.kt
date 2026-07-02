package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.network.intent.ServerIntent

/**
 * Handles a specific type of server intent.
 * Implementations should convert intents into ECS commands or game state changes.
 */
fun interface IntentHandler<T : ServerIntent> {
    fun handle(intent: T, context: IntentHandlerContext)
}

/**
 * Context provided to intent handlers for accessing server services.
 */
data class IntentHandlerContext(
    val commandBuffer: io.github.moltenmc.molten.common.ecs.command.EcsCommandBuffer,
    // TODO: Add world access, entity storage access, etc. as they become available
)
