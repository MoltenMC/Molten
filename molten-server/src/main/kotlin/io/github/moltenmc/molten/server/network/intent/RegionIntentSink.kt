package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId

fun interface RegionIntentSink {
    fun accept(worldId: WorldId, regionPos: RegionPos, intent: ServerIntent)

    companion object {
        val Noop: RegionIntentSink = RegionIntentSink { _, _, _ -> }
    }
}
