package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class TsvBedrockBlockRuntimeDataSource(
    private val input: () -> InputStream,
) : BedrockBlockRuntimeDataSource {
    override fun mappings(): Collection<BedrockRuntimeBlockMapping> =
        BufferedReader(InputStreamReader(input(), StandardCharsets.UTF_8)).useLines { lines ->
            lines.mapIndexedNotNull(::mappingFromLine).toList()
        }

    private fun mappingFromLine(index: Int, line: String): BedrockRuntimeBlockMapping? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }

        val fields = trimmed.split('\t')
        require(fields.size in MIN_FIELD_COUNT..MAX_FIELD_COUNT) {
            "Invalid Bedrock runtime mapping at line ${index + 1}: expected $MIN_FIELD_COUNT or $MAX_FIELD_COUNT tab-separated fields."
        }

        val runtimeId = fields[0].toIntOrNull()
        require(runtimeId != null && runtimeId >= 0) {
            "Invalid Bedrock runtime ID at line ${index + 1}: ${fields[0]}."
        }

        return BedrockRuntimeBlockMapping(
            runtimeId = runtimeId,
            internalState = BlockState(
                key = RegistryKey.parse(fields[1]),
                properties = fields.getOrNull(2)?.let { propertiesFromField(index, it) }.orEmpty(),
            ),
        )
    }

    private fun propertiesFromField(index: Int, field: String): Map<String, String> {
        if (field.isBlank()) {
            return emptyMap()
        }
        return field.split(',')
            .associate { entry ->
                val separator = entry.indexOf('=')
                require(separator > 0 && separator < entry.lastIndex) {
                    "Invalid Bedrock block property at line ${index + 1}: $entry."
                }
                entry.substring(0, separator) to entry.substring(separator + 1)
            }
            .toSortedMap()
    }

    companion object {
        const val DEFAULT_RESOURCE = "/molten/translator/bedrock/block_runtime.tsv"
        private const val MIN_FIELD_COUNT = 2
        private const val MAX_FIELD_COUNT = 3

        fun bundled(): TsvBedrockBlockRuntimeDataSource =
            TsvBedrockBlockRuntimeDataSource {
                TsvBedrockBlockRuntimeDataSource::class.java.getResourceAsStream(DEFAULT_RESOURCE)
                    ?: error("Missing bundled Bedrock block runtime mapping resource: $DEFAULT_RESOURCE")
            }
    }
}
