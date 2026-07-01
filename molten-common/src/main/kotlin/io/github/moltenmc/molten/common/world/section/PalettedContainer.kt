package io.github.moltenmc.molten.common.world.section

data class PalettedContainer(
    val palette: List<Int>,
    val packedData: LongArray,
) {
    override fun equals(other: Any?): Boolean =
        other is PalettedContainer &&
            palette == other.palette &&
            packedData.contentEquals(other.packedData)

    override fun hashCode(): Int = 31 * palette.hashCode() + packedData.contentHashCode()
}
