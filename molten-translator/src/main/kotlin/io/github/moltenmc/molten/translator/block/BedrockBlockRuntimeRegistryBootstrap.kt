package io.github.moltenmc.molten.translator.block

class BedrockBlockRuntimeRegistryBootstrap(
    private val dataSources: Collection<BedrockBlockRuntimeDataSource> =
        listOf(BedrockBlockRuntimeDataSource.vanillaDefaults()),
) {
    fun bootstrap(): BedrockBlockRuntimeIdTranslator {
        val mappings = linkedMapOf<Int, BedrockRuntimeBlockMapping>()
        dataSources
            .flatMap(BedrockBlockRuntimeDataSource::mappings)
            .forEach { mapping ->
                val previous = mappings[mapping.runtimeId]
                require(previous == null || previous.internalState == mapping.internalState) {
                    "Conflicting Bedrock runtime ID mapping for ${mapping.runtimeId}."
                }
                mappings[mapping.runtimeId] = mapping
            }

        return MapBackedBedrockBlockRuntimeIdTranslator(
            mappings.mapValues { (_, mapping) -> mapping.internalState },
        )
    }
}
