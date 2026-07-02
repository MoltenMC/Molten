package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId

data class RegionIntentKey(
    val worldId: WorldId,
    val regionPos: RegionPos,
)
