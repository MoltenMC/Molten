package io.github.moltenmc.molten.translator.block
import io.github.moltenmc.molten.common.world.BlockState

data class BedrockRuntimeBlockMapping(
    val runtimeId: Int,
    val internalState: BlockState,
)

fun interface BedrockBlockRuntimeDataSource {
    fun mappings(): Collection<BedrockRuntimeBlockMapping>

    companion object {
        fun static(mappings: Collection<BedrockRuntimeBlockMapping>): BedrockBlockRuntimeDataSource =
            BedrockBlockRuntimeDataSource { mappings }

        fun vanillaDefaults(): BedrockBlockRuntimeDataSource =
            TsvBedrockBlockRuntimeDataSource.bundled()
    }
}
