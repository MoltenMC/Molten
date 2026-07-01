package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.registry.RegistryKey
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TsvBedrockBlockRuntimeDataSourceTest {
    @Test
    fun readsRuntimeMappingsFromTsv() {
        val source = TsvBedrockBlockRuntimeDataSource {
            """
            # runtime_id	internal_block_key	properties
            0	minecraft:air
            1	minecraft:oak_log	axis=y,persistent=false
            """.trimIndent().byteInputStream()
        }

        val mappings = source.mappings().toList()

        assertEquals(2, mappings.size)
        assertEquals(0, mappings[0].runtimeId)
        assertEquals(RegistryKey.parse("minecraft:air"), mappings[0].internalState.key)
        assertEquals(1, mappings[1].runtimeId)
        assertEquals(RegistryKey.parse("minecraft:oak_log"), mappings[1].internalState.key)
        assertEquals(
            mapOf("axis" to "y", "persistent" to "false"),
            mappings[1].internalState.properties,
        )
    }

    @Test
    fun keepsPropertiesEmptyWhenColumnIsOmitted() {
        val source = TsvBedrockBlockRuntimeDataSource {
            "1\tminecraft:stone".byteInputStream()
        }

        val mapping = source.mappings().single()

        assertEquals(emptyMap(), mapping.internalState.properties)
    }

    @Test
    fun rejectsMalformedRows() {
        val source = TsvBedrockBlockRuntimeDataSource {
            ByteArrayInputStream("1 minecraft:stone".toByteArray(StandardCharsets.UTF_8))
        }

        assertFailsWith<IllegalArgumentException> {
            source.mappings()
        }
    }

    @Test
    fun rejectsMalformedProperties() {
        val source = TsvBedrockBlockRuntimeDataSource {
            "1\tminecraft:oak_log\taxis".byteInputStream()
        }

        assertFailsWith<IllegalArgumentException> {
            source.mappings()
        }
    }

    @Test
    fun readsBundledRuntimeMappings() {
        val mappings = TsvBedrockBlockRuntimeDataSource.bundled().mappings()

        assertEquals(RegistryKey.parse("minecraft:air"), mappings.first { it.runtimeId == 0 }.internalState.key)
        assertEquals(
            mapOf("snowy" to "false"),
            mappings.first { it.runtimeId == 2 }.internalState.properties,
        )
    }
}
