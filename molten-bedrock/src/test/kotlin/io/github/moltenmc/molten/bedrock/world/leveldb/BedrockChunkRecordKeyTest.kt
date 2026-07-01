package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BedrockChunkRecordKeyTest {
    @Test
    fun encodesOverworldKeysWithoutDimension() {
        val key = BedrockChunkRecordKey.version(ChunkPos(10, -20)).encode()
        val expected = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(10)
            .putInt(-20)
            .put(BedrockChunkRecordKey.VERSION_TAG)
            .array()

        assertContentEquals(expected, key)
    }

    @Test
    fun encodesNonOverworldKeysWithDimension() {
        val key = BedrockChunkRecordKey.version(ChunkPos(10, -20), dimension = 1).encode()
        val expected = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(10)
            .putInt(-20)
            .putInt(1)
            .put(BedrockChunkRecordKey.VERSION_TAG)
            .array()

        assertContentEquals(expected, key)
    }

    @Test
    fun encodesSubChunkKeysWithVerticalSuffix() {
        val key = BedrockChunkRecordKey.subChunk(ChunkPos(10, -20), subChunkY = -4).encode()
        val expected = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(10)
            .putInt(-20)
            .put(BedrockChunkRecordKey.SUBCHUNK_TAG)
            .put((-4).toByte())
            .array()

        assertContentEquals(expected, key)
    }

    @Test
    fun encodesData2dKeys() {
        val key = BedrockChunkRecordKey.data2d(ChunkPos(10, -20)).encode()
        val expected = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(10)
            .putInt(-20)
            .put(BedrockChunkRecordKey.DATA_2D_TAG)
            .array()

        assertContentEquals(expected, key)
    }

    @Test
    fun encodesFinalizedStateKeys() {
        val key = BedrockChunkRecordKey.finalizedState(ChunkPos(10, -20)).encode()
        val expected = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(10)
            .putInt(-20)
            .put(BedrockChunkRecordKey.FINALIZED_STATE_TAG)
            .array()

        assertContentEquals(expected, key)
    }

    @Test
    fun identifiesNonOverworldRecordTypes() {
        val position = ChunkPos(10, -20)
        val dimension = 1
        val versionKey = BedrockChunkRecordKey.version(position, dimension).encode()
        val data2dKey = BedrockChunkRecordKey.data2d(position, dimension).encode()
        val finalizedStateKey = BedrockChunkRecordKey.finalizedState(position, dimension).encode()
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = -3, dimension).encode()

        assertTrue(BedrockChunkRecordKey.isVersion(versionKey, dimension))
        assertTrue(BedrockChunkRecordKey.isData2d(data2dKey, dimension))
        assertTrue(BedrockChunkRecordKey.isFinalizedState(finalizedStateKey, dimension))
        assertEquals(-3, BedrockChunkRecordKey.subChunkY(subChunkKey, dimension))
    }

    @Test
    fun rejectsKeysFromDifferentDimensionLayouts() {
        val overworldVersionKey = BedrockChunkRecordKey.version(ChunkPos(10, -20)).encode()
        val netherVersionKey = BedrockChunkRecordKey.version(ChunkPos(10, -20), dimension = 1).encode()

        assertFalse(BedrockChunkRecordKey.isVersion(overworldVersionKey, dimension = 1))
        assertFalse(BedrockChunkRecordKey.isVersion(netherVersionKey))
        assertNull(BedrockChunkRecordKey.subChunkY(overworldVersionKey, dimension = 1))
    }

    @Test
    fun rejectsKeysFromDifferentNonOverworldDimensions() {
        val position = ChunkPos(10, -20)
        val dimensionTwoVersionKey = BedrockChunkRecordKey.version(position, dimension = 2).encode()
        val dimensionTwoData2dKey = BedrockChunkRecordKey.data2d(position, dimension = 2).encode()
        val dimensionTwoFinalizedStateKey = BedrockChunkRecordKey.finalizedState(position, dimension = 2).encode()
        val dimensionTwoSubChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 4, dimension = 2).encode()

        assertFalse(BedrockChunkRecordKey.isVersion(dimensionTwoVersionKey, dimension = 1))
        assertFalse(BedrockChunkRecordKey.isData2d(dimensionTwoData2dKey, dimension = 1))
        assertFalse(BedrockChunkRecordKey.isFinalizedState(dimensionTwoFinalizedStateKey, dimension = 1))
        assertNull(BedrockChunkRecordKey.subChunkY(dimensionTwoSubChunkKey, dimension = 1))
    }
}
