package io.github.moltenmc.molten.translator.biome

class BedrockBiomeRegistryBootstrap(
    private val dataSources: Collection<BedrockBiomeDataSource> =
        listOf(BedrockBiomeDataSource.vanillaDefaults()),
) {
    fun bootstrap(): BedrockBiomeIdTranslator {
        val mappings = linkedMapOf<Int, BedrockBiomeMapping>()
        dataSources
            .flatMap(BedrockBiomeDataSource::mappings)
            .forEach { mapping ->
                val previous = mappings[mapping.biomeId]
                require(previous == null || previous.internalKey == mapping.internalKey) {
                    "Conflicting Bedrock biome ID mapping for ${mapping.biomeId}."
                }
                mappings[mapping.biomeId] = mapping
            }

        return MapBackedBedrockBiomeIdTranslator(
            mappings.mapValues { (_, mapping) -> mapping.internalKey },
        )
    }
}
