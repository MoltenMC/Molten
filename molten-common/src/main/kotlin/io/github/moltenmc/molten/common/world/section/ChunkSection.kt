package io.github.moltenmc.molten.common.world.section

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState

data class ChunkSection(
    val y: Int,
    val blocks: PalettedContainer<BlockState>,
    val biomes: PalettedContainer<RegistryKey>,
    val light: LightData,
)
