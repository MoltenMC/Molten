package io.github.moltenmc.molten.api.entity

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.world.WorldId

interface EntityView {
    val id: EntityId

    val worldId: WorldId
}
