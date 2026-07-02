package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.StructuralChange
import io.github.moltenmc.molten.common.ecs.component.TransformComponent
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.network.intent.ServerIntent

/**
 * Handles PlayerMove intents by validating movement and updating the entity's transform component.
 */
class PlayerMoveIntentHandler(
    private val movementValidator: MovementValidator = DefaultMovementValidator(),
    private val chunkViewManager: ChunkViewManager? = null,
) : IntentHandler<ServerIntent.PlayerMove> {
    override fun handle(intent: ServerIntent.PlayerMove, context: IntentHandlerContext) {
        // Validate movement if component reader is available
        val componentReader = context.componentReader
        if (componentReader != null) {
            val currentTransform = componentReader.getComponent(
                intent.sourceEntityId,
                TransformComponent::class.java,
            ) as? TransformComponent
            
            if (currentTransform != null) {
                val validationResult = movementValidator.validate(intent, currentTransform)
                if (!validationResult.isValid) {
                    // TODO: Log invalid movement attempt
                    // TODO: Kick player for cheating if repeated violations
                    return
                }
            }
        }
        
        // Update chunk view if manager is available
        chunkViewManager?.updateChunkView(
            playerId = intent.sourceEntityId,
            newPosition = Vec3d(intent.x, intent.y, intent.z),
        )
        
        // TODO: Check if movement crosses region boundaries and trigger migration

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
