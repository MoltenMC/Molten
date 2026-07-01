package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.world.BlockState

interface BedrockBlockRuntimeIdTranslator {
    fun toInternalBlockState(runtimeId: Int): BlockState?

    fun toBedrockRuntimeId(state: BlockState): Int?
}

class MapBackedBedrockBlockRuntimeIdTranslator(
    mappings: Map<Int, BlockState>,
) : BedrockBlockRuntimeIdTranslator {
    private val runtimeToInternal = mappings.toMap()
    private val internalToRuntime = runtimeToInternal.entries.associate { (runtimeId, state) -> state to runtimeId }

    override fun toInternalBlockState(runtimeId: Int): BlockState? =
        runtimeToInternal[runtimeId]

    override fun toBedrockRuntimeId(state: BlockState): Int? =
        internalToRuntime[state]
}
