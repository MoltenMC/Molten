package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.component.TransformComponent
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.network.intent.ServerIntent

/**
 * Validates player movement intents to prevent cheating and ensure fair gameplay.
 */
interface MovementValidator {
    /**
     * Validates a movement intent against the current transform.
     * Returns true if the movement is valid, false otherwise.
     */
    fun validate(
        intent: ServerIntent.PlayerMove,
        currentTransform: TransformComponent,
    ): ValidationResult
}

/**
 * Result of movement validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val reason: String? = null,
)

/**
 * Default implementation of MovementValidator with basic anti-cheat checks.
 */
class DefaultMovementValidator(
    private val maxSpeed: Double = 10.0, // blocks per tick
    private val maxTeleportDistance: Double = 100.0, // blocks
    private val allowFlight: Boolean = false,
    private val maxVerticalSpeed: Double = 5.0, // blocks per tick
) : MovementValidator {
    override fun validate(
        intent: ServerIntent.PlayerMove,
        currentTransform: TransformComponent,
    ): ValidationResult {
        val newPosition = Vec3d(intent.x, intent.y, intent.z)
        val dx = newPosition.x - currentTransform.position.x
        val dy = newPosition.y - currentTransform.position.y
        val dz = newPosition.z - currentTransform.position.z
        val distance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        
        // Check for teleportation (too large distance)
        if (distance > maxTeleportDistance) {
            return ValidationResult(
                isValid = false,
                reason = "Movement distance $distance exceeds maximum teleport distance $maxTeleportDistance",
            )
        }
        
        // Check for speed (distance per tick)
        // TODO: This should account for tick rate and time delta
        if (distance > maxSpeed) {
            return ValidationResult(
                isValid = false,
                reason = "Movement speed $distance exceeds maximum speed $maxSpeed",
            )
        }
        
        // Check for flying (if not allowed)
        if (!allowFlight) {
            val verticalSpeed = kotlin.math.abs(dy)
            if (verticalSpeed > maxVerticalSpeed) {
                return ValidationResult(
                    isValid = false,
                    reason = "Vertical speed $verticalSpeed exceeds maximum vertical speed $maxVerticalSpeed",
                )
            }
            
            // TODO: Check if player is on ground
            // This requires ground detection component or world collision check
            // For now, we only check excessive vertical movement
        }
        
        // TODO: Add more validation:
        // - Check for noclip (through blocks) - requires world collision
        // - Check for sprinting speed limits - requires sprint state
        // - Check for elytra flight speed limits - requires equipment check
        // - Check for knockback effects - requires velocity tracking
        // - Check for vehicle movement - requires vehicle state
        
        return ValidationResult(isValid = true)
    }
}

/**
 * No-op validator that always approves movement (for testing).
 */
class NoopMovementValidator : MovementValidator {
    override fun validate(
        intent: ServerIntent.PlayerMove,
        currentTransform: TransformComponent,
    ): ValidationResult = ValidationResult(isValid = true)
}
