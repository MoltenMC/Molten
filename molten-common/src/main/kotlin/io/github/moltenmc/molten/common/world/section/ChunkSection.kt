package io.github.moltenmc.molten.common.world.section

data class ChunkSection(
    val y: Int,
    val blocks: PalettedContainer,
    val biomes: PalettedContainer,
    val light: LightData,
)
