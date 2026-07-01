package io.github.moltenmc.molten.api.world

import io.github.moltenmc.molten.common.world.WorldId

interface World {
    val id: WorldId

    val name: String
}
