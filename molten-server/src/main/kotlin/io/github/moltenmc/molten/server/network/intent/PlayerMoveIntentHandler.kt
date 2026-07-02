package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.StructuralChange
import io.github.moltenmc.molten.common.ecs.component.TransformComponent
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.network.intent.ServerIntent

/**
 * Handles PlayerMove intents by updating the entity's transform component.
 */
class PlayerMoveIntentHandler : IntentHandler<ServerIntent.PlayerMove> {
    override fun handle(intent: ServerIntent.PlayerMove, context: IntentHandlerContext) {
        // TODO: Add movement validation before updating position
        // TODO: Check if movement crosses region boundaries and trigger migration
        // TODO: Update chunk view distance based on new position

        val newTransform = TransformComponent(
            position = Vec3d(intent.x, intent.y, intent.z),
            yaw = intent.yaw,
            pitch = intent.pitch,
        )

        context.commandBuffer.enqueue(
            StructuralChange.SetComponent(
                entityId = intent.sourceEntityId,
                component = newTransform,
            ),
        )
    }
}
