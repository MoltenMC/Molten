package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.Chunk
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
}
