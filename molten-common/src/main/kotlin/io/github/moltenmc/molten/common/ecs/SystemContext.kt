package io.github.moltenmc.molten.common.ecs

import io.github.moltenmc.molten.common.world.WorldId
import java.time.Duration

data class SystemContext(
    val worldId: WorldId,
    val deltaTime: Duration,
    val structuralChanges: StructuralChangeBuffer,
) {
    init {
        require(!deltaTime.isNegative) { "Delta time must be non-negative." }
    }
}
