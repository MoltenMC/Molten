package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoadedChunkStoreTest {
    @Test
    fun storesAndRemovesLoadedChunksByKey() {
        val store = LoadedChunkStore()
        val key = key()
        val chunk = Chunk(key.position, emptyList())

        assertFalse(store.contains(key))
        assertNull(store.put(key, chunk))
        assertTrue(store.contains(key))
        assertEquals(chunk, store.get(key))
        assertEquals(setOf(key), store.keys())
        assertEquals(chunk, store.remove(key))
        assertFalse(store.contains(key))
    }

    @Test
    fun replacesExistingChunkAndReturnsPreviousValue() {
        val store = LoadedChunkStore()
        val key = key()
        val first = Chunk(key.position, emptyList(), dataVersion = 1)
        val second = Chunk(key.position, emptyList(), dataVersion = 2)

        store.put(key, first)

        assertEquals(first, store.put(key, second))
        assertEquals(second, store.get(key))
    }

    private fun key(): ChunkKey =
        ChunkKey(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(1, 2),
        )
}
