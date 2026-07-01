package io.github.moltenmc.molten.common.world.section

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PalettedContainerTest {
    @Test
    fun readsPaletteIndexesFromPackedWords() {
        val container = PalettedContainer(
            palette = listOf("stone", "dirt"),
            packedData = longArrayOf(0b10),
            bitsPerEntry = 1,
            wordBits = Int.SIZE_BITS,
        )

        assertEquals(0, container.paletteIndexAt(0))
        assertEquals(1, container.paletteIndexAt(1))
        assertEquals("stone", container.valueAt(0))
        assertEquals("dirt", container.valueAt(1))
    }

    @Test
    fun returnsFirstPaletteEntryForSingleValueContainers() {
        val container = PalettedContainer(
            palette = listOf("air"),
            packedData = longArrayOf(0),
        )

        assertEquals(0, container.paletteIndexAt(4095))
        assertEquals("air", container.valueAt(4095))
    }

    @Test
    fun rejectsOutOfRangePackedIndexes() {
        val container = PalettedContainer(
            palette = listOf("stone"),
            packedData = longArrayOf(0),
            bitsPerEntry = 1,
            wordBits = Int.SIZE_BITS,
        )

        assertFailsWith<IllegalArgumentException> {
            container.paletteIndexAt(32)
        }
    }

    @Test
    fun rejectsInvalidPackingMetadata() {
        assertFailsWith<IllegalArgumentException> {
            PalettedContainer(
                palette = listOf("stone"),
                packedData = longArrayOf(0),
                bitsPerEntry = -1,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            PalettedContainer(
                palette = listOf("stone"),
                packedData = longArrayOf(0),
                wordBits = 16,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            PalettedContainer(
                palette = listOf("stone"),
                packedData = longArrayOf(0),
                bitsPerEntry = Long.SIZE_BITS + 1,
            )
        }
    }

    @Test
    fun rejectsPaletteIndexesOutsidePaletteSize() {
        val container = PalettedContainer(
            palette = listOf("stone"),
            packedData = longArrayOf(1),
            bitsPerEntry = 1,
            wordBits = Int.SIZE_BITS,
        )

        assertFailsWith<IllegalArgumentException> {
            container.valueAt(0)
        }
    }

    @Test
    fun rejectsNegativeIndexes() {
        val container = PalettedContainer(
            palette = listOf("stone"),
            packedData = longArrayOf(0),
            bitsPerEntry = 1,
            wordBits = Int.SIZE_BITS,
        )

        assertFailsWith<IllegalArgumentException> {
            container.paletteIndexAt(-1)
        }
    }

    @Test
    fun readsFullWidthLongEntriesWithoutShiftOverflow() {
        val container = PalettedContainer(
            palette = listOf("zero"),
            packedData = longArrayOf(-1L),
            bitsPerEntry = Long.SIZE_BITS,
            wordBits = Long.SIZE_BITS,
        )

        assertEquals(-1L, container.paletteIndexLongAt(0))
        assertFailsWith<IllegalArgumentException> {
            container.paletteIndexAt(0)
        }
    }
}
