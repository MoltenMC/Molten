package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals

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
}
