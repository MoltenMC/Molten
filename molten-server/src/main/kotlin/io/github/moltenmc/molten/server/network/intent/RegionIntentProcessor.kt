package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.command.EcsCommandBuffer

/**
 * Processes region intent batches using registered intent handlers.
 */
interface RegionIntentProcessor {
    fun process(batch: RegionIntentBatch)
}

/**
 * Default implementation of RegionIntentProcessor that uses registered intent handlers.
 */
class DefaultRegionIntentProcessor(
    private val handlerRegistry: IntentHandlerRegistry,
    private val commandBuffer: EcsCommandBuffer,
    private val componentReader: ComponentReader? = null,
) : RegionIntentProcessor {
    override fun process(batch: RegionIntentBatch) {
        val context = IntentHandlerContext(commandBuffer, componentReader)
        batch.intents.forEach { intent ->
            handlerRegistry.handle(intent, context)
        }
    }
}

/**
 * No-op implementation of RegionIntentProcessor for testing and scaffolding.
 */
class NoopRegionIntentProcessor : RegionIntentProcessor {
    override fun process(batch: RegionIntentBatch) {
        // No-op
    }
}
