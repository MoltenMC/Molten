package io.github.moltenmc.molten.common.world.chunk

enum class DirtyMarker {
    BLOCK_DIRTY,
    LIGHTING_DIRTY,
    BIOME_DIRTY,
    HEIGHT_MAP_DIRTY,
    BLOCK_ENTITY_DIRTY,
    NEEDS_SAVE,
}
