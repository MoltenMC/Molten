package io.github.moltenmc.molten.server.network.intent

fun interface RegionIntentProcessor {
    fun process(batch: RegionIntentBatch)

    companion object {
        // TODO: Replace with actual intent processor implementation
        // This is temporary scaffolding until intent processing logic is implemented
        val Noop: RegionIntentProcessor = RegionIntentProcessor { }
    }
}
