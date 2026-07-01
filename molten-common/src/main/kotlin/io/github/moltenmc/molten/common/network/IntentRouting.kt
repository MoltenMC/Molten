package io.github.moltenmc.molten.common.network

import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId

data class IntentRouting(
    val worldId: WorldId?,
    val regionPos: RegionPos?,
)
