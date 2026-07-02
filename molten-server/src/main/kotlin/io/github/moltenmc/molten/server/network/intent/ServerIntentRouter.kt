package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.network.intent.ServerIntentSink

class ServerIntentRouter(
    private val regionSink: RegionIntentSink = RegionIntentSink.Noop,
    private val globalSink: ServerIntentSink = ServerIntentSink.Noop,
) : ServerIntentSink {
    override fun accept(intent: ServerIntent) {
        val worldId = intent.routing.worldId
        val regionPos = intent.routing.regionPos
        if (worldId != null && regionPos != null) {
            regionSink.accept(worldId, regionPos, intent)
        } else {
            globalSink.accept(intent)
        }
    }
}
