package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.chunk.Chunk
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import io.github.moltenmc.molten.common.registry.RegistryKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultLevelDbStorageAdapterTest {
    @Test
    fun returnsNullForMissingChunk() {
        val databasePath = Files.createTempDirectory("molten-leveldb-missing")
        DefaultLevelDbStorageAdapter(databasePath).use { adapter ->
            assertNull(adapter.loadChunk(ChunkPos(0, 0)).get())
        }
    }

    @Test
    fun savesAndLoadsRawChunkRecords() {
        val databasePath = Files.createTempDirectory("molten-leveldb-roundtrip")
        DefaultLevelDbStorageAdapter(databasePath).use { adapter ->
            val position = ChunkPos(4, 8)
            val versionKey = BedrockChunkRecordKey.version(position).encode()
            val terrainKey = BedrockChunkRecordKey(
                position = position,
                dimension = BedrockChunkRecordKey.OVERWORLD_DIMENSION,
                tag = BedrockChunkRecordKey.LEGACY_TERRAIN_TAG,
            ).encode()
            val chunk = RawPreservingLevelDbChunkMapper().toChunk(
                position,
                mapOf(
                    versionKey to byteArrayOf(9),
                    terrainKey to byteArrayOf(1, 2, 3),
                ),
            )

            adapter.saveChunk(chunk).get()
            val loaded = adapter.loadChunk(position).get()

            assertEquals(position, loaded?.position)
            val records = RawPreservingLevelDbChunkMapper().toRecords(loaded ?: Chunk(position, emptyList()))
            assertEquals(2, records.size)
            assertContentEquals(byteArrayOf(9), records.entries.first { it.key.contentEquals(versionKey) }.value)
            assertContentEquals(byteArrayOf(1, 2, 3), records.entries.first { it.key.contentEquals(terrainKey) }.value)
        }
    }

    @Test
    fun savesGeneratedChunkWithDefaultVersionRecord() {
        val databasePath = Files.createTempDirectory("molten-leveldb-generated")
        DefaultLevelDbStorageAdapter(databasePath).use { adapter ->
            val position = ChunkPos(2, 3)
            val chunk = Chunk(
                position = position,
                sections = listOf(emptySection()),
            )

            adapter.saveChunk(chunk).get()
            val loaded = adapter.loadChunk(position).get()

            assertEquals(position, loaded?.position)
            val records = RawPreservingLevelDbChunkMapper().toRecords(loaded ?: Chunk(position, emptyList()))
            assertContentEquals(
                byteArrayOf(8),
                records.entries.first { it.key.contentEquals(BedrockChunkRecordKey.version(position).encode()) }.value,
            )
        }
    }

    @Test
    fun savesAndLoadsGeneratedChunkInNonOverworldDimension() {
        val databasePath = Files.createTempDirectory("molten-leveldb-dimension")
        val dimension = 1
        DefaultLevelDbStorageAdapter(databasePath, dimension = dimension).use { adapter ->
            val position = ChunkPos(7, -9)
            val chunk = Chunk(
                position = position,
                sections = listOf(emptySection()),
            )

            adapter.saveChunk(chunk).get()
            val loaded = adapter.loadChunk(position).get()

            assertEquals(position, loaded?.position)
            assertEquals(1, loaded?.sections?.size)
            val records = RawPreservingLevelDbChunkMapper(dimension = dimension)
                .toRecords(loaded ?: Chunk(position, emptyList()))
            assertContentEquals(
                byteArrayOf(8),
                records.entries.single {
                    it.key.contentEquals(BedrockChunkRecordKey.version(position, dimension).encode())
                }.value,
            )
        }
    }

    @Test
    fun removesStaleChunkRecordsWhenSavingSmallerRecordSet() {
        val databasePath = Files.createTempDirectory("molten-leveldb-stale")
        DefaultLevelDbStorageAdapter(databasePath).use { adapter ->
            val position = ChunkPos(5, 6)
            adapter.saveChunk(
                Chunk(
                    position = position,
                    sections = listOf(emptySection()),
                ),
            ).get()

            adapter.saveChunk(Chunk(position = position, sections = emptyList())).get()
            val loaded = adapter.loadChunk(position).get()

            assertEquals(emptyList(), loaded?.sections)
            val records = RawPreservingLevelDbChunkMapper().toRecords(loaded ?: Chunk(position, emptyList()))
            assertEquals(1, records.size)
            assertContentEquals(
                byteArrayOf(8),
                records.entries.single { it.key.contentEquals(BedrockChunkRecordKey.version(position).encode()) }.value,
            )
        }
    }

    private fun emptySection(): ChunkSection =
        ChunkSection(
            y = 0,
            blocks = PalettedContainer(
                palette = listOf(BlockState(RegistryKey.parse("minecraft:air"))),
                packedData = longArrayOf(0),
            ),
            biomes = PalettedContainer(
                palette = listOf(RegistryKey.parse("minecraft:plains")),
                packedData = longArrayOf(0),
            ),
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )
}
