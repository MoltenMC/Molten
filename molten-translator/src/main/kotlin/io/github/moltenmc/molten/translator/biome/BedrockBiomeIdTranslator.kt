package io.github.moltenmc.molten.translator.biome

import io.github.moltenmc.molten.common.registry.RegistryKey

interface BedrockBiomeIdTranslator {
    fun toInternalBiomeKey(biomeId: Int): RegistryKey?

    fun toBedrockBiomeId(key: RegistryKey): Int?
}

class MapBackedBedrockBiomeIdTranslator(
    mappings: Map<Int, RegistryKey>,
) : BedrockBiomeIdTranslator {
    private val bedrockToInternal = mappings.toMap()
    private val internalToBedrock = bedrockToInternal.entries.associate { (biomeId, key) -> key to biomeId }

    override fun toInternalBiomeKey(biomeId: Int): RegistryKey? =
        bedrockToInternal[biomeId]

    override fun toBedrockBiomeId(key: RegistryKey): Int? =
        internalToBedrock[key]
}
