package io.github.moltenmc.molten.common.world.section

data class PalettedContainer<T>(
    val palette: List<T>,
    val packedData: LongArray,
) {
    override fun equals(other: Any?): Boolean =
        other is PalettedContainer<*> &&
            palette == other.palette &&
            packedData.contentEquals(other.packedData)

    override fun hashCode(): Int = 31 * palette.hashCode() + packedData.contentHashCode()
}
