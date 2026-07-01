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
            # runtime_id	internal_block_key
            0	minecraft:air
            1	minecraft:stone
            """.trimIndent().byteInputStream()
        }

        val mappings = source.mappings().toList()

        assertEquals(2, mappings.size)
        assertEquals(0, mappings[0].runtimeId)
        assertEquals(RegistryKey.parse("minecraft:air"), mappings[0].internalState.key)
        assertEquals(1, mappings[1].runtimeId)
        assertEquals(RegistryKey.parse("minecraft:stone"), mappings[1].internalState.key)
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
    fun readsBundledRuntimeMappings() {
        val mappings = TsvBedrockBlockRuntimeDataSource.bundled().mappings()

        assertEquals(RegistryKey.parse("minecraft:air"), mappings.first { it.runtimeId == 0 }.internalState.key)
    }
}
