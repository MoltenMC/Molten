package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BedrockBlockRuntimeRegistryBootstrapTest {
    @Test
    fun bootstrapsRuntimeTranslatorFromDataSources() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val bootstrap = BedrockBlockRuntimeRegistryBootstrap(
            listOf(
                BedrockBlockRuntimeDataSource.static(
                    listOf(BedrockRuntimeBlockMapping(1, stone)),
                ),
            ),
        )

        val translator = bootstrap.bootstrap()

        assertEquals(stone, translator.toInternalBlockState(1))
        assertEquals(1, translator.toBedrockRuntimeId(stone))
    }

    @Test
    fun rejectsConflictingRuntimeIds() {
        val bootstrap = BedrockBlockRuntimeRegistryBootstrap(
            listOf(
                BedrockBlockRuntimeDataSource.static(
                    listOf(BedrockRuntimeBlockMapping(1, BlockState(RegistryKey.parse("minecraft:stone")))),
                ),
                BedrockBlockRuntimeDataSource.static(
                    listOf(BedrockRuntimeBlockMapping(1, BlockState(RegistryKey.parse("minecraft:dirt")))),
                ),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            bootstrap.bootstrap()
        }
    }
}
