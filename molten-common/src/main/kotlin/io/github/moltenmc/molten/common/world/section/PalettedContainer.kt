package io.github.moltenmc.molten.common.world.section

data class PalettedContainer<T>(
    val palette: List<T>,
    val packedData: LongArray,
    val bitsPerEntry: Int = 0,
    val wordBits: Int = Long.SIZE_BITS,
) {
    init {
        require(bitsPerEntry >= 0) { "bitsPerEntry must not be negative." }
        require(wordBits == Int.SIZE_BITS || wordBits == Long.SIZE_BITS) {
            "wordBits must be ${Int.SIZE_BITS} or ${Long.SIZE_BITS}."
        }
        require(bitsPerEntry == 0 || bitsPerEntry <= wordBits) {
            "bitsPerEntry must fit inside wordBits."
        }
    }

    fun paletteIndexAt(index: Int): Int {
        val paletteIndex = paletteIndexLongAt(index)
        require(paletteIndex in 0..Int.MAX_VALUE.toLong()) {
            "Palette index $paletteIndex is outside Int range."
        }
        return paletteIndex.toInt()
    }

    fun paletteIndexLongAt(index: Int): Long {
        require(index >= 0) { "Palette index position must not be negative." }
        if (bitsPerEntry == 0) {
            return 0L
        }

        val entriesPerWord = wordBits / bitsPerEntry
        require(entriesPerWord > 0) { "bitsPerEntry must allow at least one entry per word." }

        val wordIndex = index / entriesPerWord
        require(wordIndex < packedData.size) { "Palette index position $index exceeds packed data size." }

        val bitOffset = (index % entriesPerWord) * bitsPerEntry
        val mask = if (bitsPerEntry == Long.SIZE_BITS) -1L else (1L shl bitsPerEntry) - 1L
        return (packedData[wordIndex] ushr bitOffset) and mask
    }

    fun valueAt(index: Int): T {
        val paletteIndex = paletteIndexAt(index)
        require(paletteIndex < palette.size) { "Palette index $paletteIndex exceeds palette size." }
        return palette[paletteIndex]
    }

    override fun equals(other: Any?): Boolean =
        other is PalettedContainer<*> &&
            palette == other.palette &&
            packedData.contentEquals(other.packedData) &&
            bitsPerEntry == other.bitsPerEntry &&
            wordBits == other.wordBits

    override fun hashCode(): Int {
        var result = palette.hashCode()
        result = 31 * result + packedData.contentHashCode()
        result = 31 * result + bitsPerEntry
        result = 31 * result + wordBits
        return result
    }
}
