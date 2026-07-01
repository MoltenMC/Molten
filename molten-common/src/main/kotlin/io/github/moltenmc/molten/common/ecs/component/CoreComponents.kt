package io.github.moltenmc.molten.common.ecs.component

import io.github.moltenmc.molten.common.ecs.Component
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId

data class TransformComponent(val position: Vec3d, val yaw: Float = 0f, val pitch: Float = 0f) : Component

data class VelocityComponent(val velocity: Vec3d) : Component

data class WorldComponent(val worldId: WorldId) : Component

data class RegionComponent(val regionPos: RegionPos) : Component
