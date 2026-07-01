package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MapBackedBedrockBlockRuntimeIdTranslatorTest {
    @Test
    fun resolvesRuntimeIdsToInternalBlockStates() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val translator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone))

        assertEquals(stone, translator.toInternalBlockState(1))
        assertNull(translator.toInternalBlockState(2))
    }

    @Test
    fun resolvesInternalBlockStatesToRuntimeIds() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val translator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone))

        assertEquals(1, translator.toBedrockRuntimeId(stone))
        assertNull(translator.toBedrockRuntimeId(BlockState(RegistryKey.parse("minecraft:dirt"))))
    }
}
