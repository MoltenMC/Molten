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
        require(fields.size == FIELD_COUNT) {
            "Invalid Bedrock runtime mapping at line ${index + 1}: expected $FIELD_COUNT tab-separated fields."
        }

        val runtimeId = fields[0].toIntOrNull()
        require(runtimeId != null && runtimeId >= 0) {
            "Invalid Bedrock runtime ID at line ${index + 1}: ${fields[0]}."
        }

        return BedrockRuntimeBlockMapping(
            runtimeId = runtimeId,
            internalState = BlockState(RegistryKey.parse(fields[1])),
        )
    }

    companion object {
        const val DEFAULT_RESOURCE = "/molten/translator/bedrock/block_runtime.tsv"
        private const val FIELD_COUNT = 2

        fun bundled(): TsvBedrockBlockRuntimeDataSource =
            TsvBedrockBlockRuntimeDataSource {
                TsvBedrockBlockRuntimeDataSource::class.java.getResourceAsStream(DEFAULT_RESOURCE)
                    ?: error("Missing bundled Bedrock block runtime mapping resource: $DEFAULT_RESOURCE")
            }
    }
}
