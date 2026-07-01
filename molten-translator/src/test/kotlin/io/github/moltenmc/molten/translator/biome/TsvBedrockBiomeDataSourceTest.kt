package io.github.moltenmc.molten.translator.biome

import io.github.moltenmc.molten.common.registry.RegistryKey
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TsvBedrockBiomeDataSourceTest {
    @Test
    fun readsBiomeMappingsFromTsv() {
        val source = TsvBedrockBiomeDataSource {
            """
            # biome_id	internal_biome_key
            1	minecraft:plains
            4	minecraft:forest
            """.trimIndent().byteInputStream()
        }

        val mappings = source.mappings().toList()

        assertEquals(2, mappings.size)
        assertEquals(1, mappings[0].biomeId)
        assertEquals(RegistryKey.parse("minecraft:plains"), mappings[0].internalKey)
        assertEquals(4, mappings[1].biomeId)
        assertEquals(RegistryKey.parse("minecraft:forest"), mappings[1].internalKey)
    }

    @Test
    fun rejectsMalformedRows() {
        val source = TsvBedrockBiomeDataSource {
            ByteArrayInputStream("1 minecraft:plains".toByteArray(StandardCharsets.UTF_8))
        }

        assertFailsWith<IllegalArgumentException> {
            source.mappings()
        }
    }

    @Test
    fun rejectsBiomeIdsOutsideByteRange() {
        val source = TsvBedrockBiomeDataSource {
            "300\tminecraft:plains".byteInputStream()
        }

        assertFailsWith<IllegalArgumentException> {
            source.mappings()
        }
    }

    @Test
    fun readsBundledBiomeMappings() {
        val mappings = TsvBedrockBiomeDataSource.bundled().mappings()

        assertEquals(RegistryKey.parse("minecraft:plains"), mappings.first { it.biomeId == 1 }.internalKey)
    }
}
