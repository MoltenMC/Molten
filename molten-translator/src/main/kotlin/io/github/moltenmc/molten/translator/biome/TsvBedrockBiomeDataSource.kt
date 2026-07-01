package io.github.moltenmc.molten.translator.biome

import io.github.moltenmc.molten.common.registry.RegistryKey
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class TsvBedrockBiomeDataSource(
    private val input: () -> InputStream,
) : BedrockBiomeDataSource {
    override fun mappings(): Collection<BedrockBiomeMapping> =
        BufferedReader(InputStreamReader(input(), StandardCharsets.UTF_8)).useLines { lines ->
            lines.mapIndexedNotNull(::mappingFromLine).toList()
        }

    private fun mappingFromLine(index: Int, line: String): BedrockBiomeMapping? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }

        val fields = trimmed.split('\t')
        require(fields.size == FIELD_COUNT) {
            "Invalid Bedrock biome mapping at line ${index + 1}: expected $FIELD_COUNT tab-separated fields."
        }

        val biomeId = fields[0].toIntOrNull()
        require(biomeId != null && biomeId in 0..0xff) {
            "Invalid Bedrock biome ID at line ${index + 1}: ${fields[0]}."
        }

        return BedrockBiomeMapping(
            biomeId = biomeId,
            internalKey = RegistryKey.parse(fields[1]),
        )
    }

    companion object {
        const val DEFAULT_RESOURCE = "/molten/translator/bedrock/biomes.tsv"
        private const val FIELD_COUNT = 2

        fun bundled(): TsvBedrockBiomeDataSource =
            TsvBedrockBiomeDataSource {
                TsvBedrockBiomeDataSource::class.java.getResourceAsStream(DEFAULT_RESOURCE)
                    ?: error("Missing bundled Bedrock biome mapping resource: $DEFAULT_RESOURCE")
            }
    }
}
