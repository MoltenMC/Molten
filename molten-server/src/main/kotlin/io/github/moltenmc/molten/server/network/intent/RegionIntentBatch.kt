package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.network.intent.ServerIntent

data class RegionIntentBatch(
    val key: RegionIntentKey,
    val intents: List<ServerIntent>,
)
