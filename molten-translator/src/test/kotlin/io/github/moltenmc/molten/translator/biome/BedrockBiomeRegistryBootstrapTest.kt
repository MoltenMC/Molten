package io.github.moltenmc.molten.translator.biome

import io.github.moltenmc.molten.common.registry.RegistryKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BedrockBiomeRegistryBootstrapTest {
    @Test
    fun bootstrapsBiomeTranslatorFromDataSources() {
        val forest = RegistryKey.parse("minecraft:forest")
        val bootstrap = BedrockBiomeRegistryBootstrap(
            listOf(
                BedrockBiomeDataSource.static(
                    listOf(BedrockBiomeMapping(4, forest)),
                ),
            ),
        )

        val translator = bootstrap.bootstrap()

        assertEquals(forest, translator.toInternalBiomeKey(4))
        assertEquals(4, translator.toBedrockBiomeId(forest))
        assertNull(translator.toInternalBiomeKey(5))
    }

    @Test
    fun rejectsConflictingBiomeIds() {
        val bootstrap = BedrockBiomeRegistryBootstrap(
            listOf(
                BedrockBiomeDataSource.static(
                    listOf(BedrockBiomeMapping(4, RegistryKey.parse("minecraft:forest"))),
                ),
                BedrockBiomeDataSource.static(
                    listOf(BedrockBiomeMapping(4, RegistryKey.parse("minecraft:plains"))),
                ),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            bootstrap.bootstrap()
        }
    }
}
