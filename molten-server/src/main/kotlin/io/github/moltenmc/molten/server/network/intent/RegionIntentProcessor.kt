package io.github.moltenmc.molten.server.network.intent

fun interface RegionIntentProcessor {
    fun process(batch: RegionIntentBatch)

    companion object {
        val Noop: RegionIntentProcessor = RegionIntentProcessor { }
    }
}
