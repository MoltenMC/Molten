package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.world.ChunkPos
import kotlin.test.Test
import kotlin.test.assertEquals

class AnvilChunkCoordinatesTest {
    @Test
    fun mapsPositiveChunkCoordinatesToRegionAndLocalCoordinates() {
        val coordinates = AnvilChunkCoordinates.fromChunkPos(ChunkPos(33, 65))

        assertEquals(1, coordinates.regionX)
        assertEquals(2, coordinates.regionZ)
        assertEquals(1, coordinates.localX)
        assertEquals(1, coordinates.localZ)
    }

    @Test
    fun mapsNegativeChunkCoordinatesToRegionAndLocalCoordinates() {
        val coordinates = AnvilChunkCoordinates.fromChunkPos(ChunkPos(-1, -33))

        assertEquals(-1, coordinates.regionX)
        assertEquals(-2, coordinates.regionZ)
        assertEquals(31, coordinates.localX)
        assertEquals(31, coordinates.localZ)
    }
}
