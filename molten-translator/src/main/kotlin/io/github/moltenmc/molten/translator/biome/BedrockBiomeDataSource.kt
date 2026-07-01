package io.github.moltenmc.molten.translator.biome

import io.github.moltenmc.molten.common.registry.RegistryKey

data class BedrockBiomeMapping(
    val biomeId: Int,
    val internalKey: RegistryKey,
)

fun interface BedrockBiomeDataSource {
    fun mappings(): Collection<BedrockBiomeMapping>

    companion object {
        fun static(mappings: Collection<BedrockBiomeMapping>): BedrockBiomeDataSource =
            BedrockBiomeDataSource { mappings }

        fun vanillaDefaults(): BedrockBiomeDataSource =
            TsvBedrockBiomeDataSource.bundled()
    }
}
