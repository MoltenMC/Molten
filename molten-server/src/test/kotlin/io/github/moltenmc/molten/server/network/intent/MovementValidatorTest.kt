package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.ecs.component.TransformComponent
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MovementValidatorTest {
    @Test
    fun validatesNormalMovement() {
        val validator = DefaultMovementValidator()
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(1.0, 64.0, 1.0)
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun rejectsExcessiveSpeed() {
        val validator = DefaultMovementValidator(maxSpeed = 5.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(10.0, 64.0, 0.0) // 10 blocks in one tick
        
        val result = validator.validate(intent, currentTransform)
        
        assertFalse(result.isValid)
        assertTrue(result.reason?.contains("speed") == true)
    }

    @Test
    fun rejectsTeleportation() {
        val validator = DefaultMovementValidator(maxTeleportDistance = 50.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(100.0, 64.0, 0.0) // 100 blocks away
        
        val result = validator.validate(intent, currentTransform)
        
        assertFalse(result.isValid)
        assertTrue(result.reason?.contains("teleport") == true)
    }

    @Test
    fun acceptsMovementAtMaxSpeed() {
        val validator = DefaultMovementValidator(maxSpeed = 5.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(5.0, 64.0, 0.0) // Exactly at max speed
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun acceptsMovementAtMaxTeleportDistance() {
        val validator = DefaultMovementValidator(maxSpeed = 100.0, maxTeleportDistance = 100.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(100.0, 64.0, 0.0) // Exactly at max distance
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun noopValidatorAlwaysApproves() {
        val validator = NoopMovementValidator()
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(1000.0, 64.0, 1000.0) // Very far
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun validatesVerticalMovement() {
        val validator = DefaultMovementValidator(maxSpeed = 5.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(0.0, 70.0, 0.0) // 6 blocks up
        
        val result = validator.validate(intent, currentTransform)
        
        assertFalse(result.isValid)
    }

    @Test
    fun validatesDiagonalMovement() {
        val validator = DefaultMovementValidator(maxSpeed = 5.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(3.0, 64.0, 4.0) // 5 blocks diagonal (3-4-5 triangle)
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun rejectsExcessiveVerticalMovementWhenFlightDisabled() {
        val validator = DefaultMovementValidator(allowFlight = false, maxVerticalSpeed = 3.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(0.0, 70.0, 0.0) // 6 blocks up
        
        val result = validator.validate(intent, currentTransform)
        
        assertFalse(result.isValid)
        assertTrue(result.reason?.contains("vertical") == true)
    }

    @Test
    fun allowsVerticalMovementWhenFlightEnabled() {
        val validator = DefaultMovementValidator(allowFlight = true, maxVerticalSpeed = 3.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(0.0, 70.0, 0.0) // 6 blocks up
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun acceptsNormalVerticalMovement() {
        val validator = DefaultMovementValidator(allowFlight = false, maxVerticalSpeed = 3.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 64.0, 0.0))
        val intent = moveIntent(0.0, 66.0, 0.0) // 2 blocks up
        
        val result = validator.validate(intent, currentTransform)
        
        assertTrue(result.isValid)
    }

    @Test
    fun rejectsDownwardFallingTooFast() {
        val validator = DefaultMovementValidator(allowFlight = false, maxSpeed = 100.0, maxVerticalSpeed = 10.0)
        val currentTransform = TransformComponent(Vec3d(0.0, 100.0, 0.0))
        val intent = moveIntent(0.0, 50.0, 0.0) // 50 blocks down (too fast)
        
        val result = validator.validate(intent, currentTransform)
        
        assertFalse(result.isValid)
        assertTrue(result.reason?.contains("vertical") == true)
    }

    private fun moveIntent(x: Double, y: Double, z: Double): ServerIntent.PlayerMove =
        ServerIntent.PlayerMove(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = IntentRouting(WorldId(UUID(0, 1)), RegionPos(0, 0)),
            x = x,
            y = y,
            z = z,
            yaw = 0f,
            pitch = 0f,
        )
}
