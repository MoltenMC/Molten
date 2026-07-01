package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.chunk.Chunk
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
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

    @Test
    fun overwritesExistingChunkDataWhenSavingEmptyChunk() {
        val regionDirectory = Files.createTempDirectory("molten-anvil-overwrite")
        DefaultAnvilStorageAdapter(regionDirectory).use { adapter ->
            val position = ChunkPos(3, 5)

            adapter.saveChunk(Chunk(position, listOf(section()))).get()
            adapter.saveChunk(Chunk(position, emptyList())).get()
            val loaded = adapter.loadChunk(position).get()

            assertEquals(position, loaded?.position)
            assertEquals(emptyList(), loaded?.sections)
        }
    }

    private fun section(): ChunkSection =
        ChunkSection(
            y = 0,
            blocks = PalettedContainer(
                palette = listOf(BlockState(RegistryKey.parse("minecraft:stone"))),
                packedData = LongArray(0),
            ),
            biomes = PalettedContainer(
                palette = listOf(RegistryKey.parse("minecraft:plains")),
                packedData = LongArray(0),
            ),
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )
}
