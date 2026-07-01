package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.Chunk
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultAnvilStorageAdapterTest {
    @Test
    fun returnsNullForMissingChunk() {
        val regionDirectory = Files.createTempDirectory("molten-anvil-missing")
        DefaultAnvilStorageAdapter(regionDirectory).use { adapter ->
            assertNull(adapter.loadChunk(ChunkPos(0, 0)).get())
        }
    }

    @Test
    fun savesAndLoadsChunkSkeleton() {
        val regionDirectory = Files.createTempDirectory("molten-anvil-roundtrip")
        DefaultAnvilStorageAdapter(regionDirectory).use { adapter ->
            val chunk = Chunk(ChunkPos(3, 5), emptyList())

            adapter.saveChunk(chunk).get()
            val loaded = adapter.loadChunk(chunk.position).get()

            assertEquals(chunk.position, loaded?.position)
            assertEquals(emptyList(), loaded?.sections)
        }
    }
}
