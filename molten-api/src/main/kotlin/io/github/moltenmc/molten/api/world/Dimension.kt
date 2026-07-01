package io.github.moltenmc.molten.api.world

import io.github.moltenmc.molten.common.world.DimensionId

interface Dimension {
    val id: DimensionId

    val name: String
}
