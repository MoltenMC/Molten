package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BedrockChunkRecordKey(
    val position: ChunkPos,
    val dimension: Int,
    val tag: Byte,
    val suffix: ByteArray = ByteArray(0),
) {
    fun encode(): ByteArray {
        val buffer = if (dimension == OVERWORLD_DIMENSION) {
            ByteBuffer.allocate(OVERWORLD_KEY_BYTES + suffix.size)
        } else {
            ByteBuffer.allocate(DIMENSION_KEY_BYTES + suffix.size)
        }.order(ByteOrder.LITTLE_ENDIAN)

        buffer.putInt(position.x)
        buffer.putInt(position.z)
        if (dimension != OVERWORLD_DIMENSION) {
            buffer.putInt(dimension)
        }
        buffer.put(tag)
        buffer.put(suffix)
        return buffer.array()
    }

    override fun equals(other: Any?): Boolean =
        other is BedrockChunkRecordKey &&
            position == other.position &&
            dimension == other.dimension &&
            tag == other.tag &&
            suffix.contentEquals(other.suffix)

    override fun hashCode(): Int =
        31 * (31 * (31 * position.hashCode() + dimension) + tag) + suffix.contentHashCode()

    override fun toString(): String =
        "BedrockChunkRecordKey(position=$position, dimension=$dimension, tag=$tag, suffix=${suffix.contentToString()})"

    companion object {
        const val OVERWORLD_DIMENSION = 0
        const val DATA_2D_TAG: Byte = 0x2d
        const val VERSION_TAG: Byte = 0x2c
        const val SUBCHUNK_TAG: Byte = 0x2f
        const val FINALIZED_STATE_TAG: Byte = 0x36
        const val LEGACY_TERRAIN_TAG: Byte = 0x30

        private const val OVERWORLD_KEY_BYTES = 9
        private const val DIMENSION_KEY_BYTES = 13

        fun version(position: ChunkPos, dimension: Int = OVERWORLD_DIMENSION): BedrockChunkRecordKey =
            BedrockChunkRecordKey(position, dimension, VERSION_TAG)

        fun data2d(position: ChunkPos, dimension: Int = OVERWORLD_DIMENSION): BedrockChunkRecordKey =
            BedrockChunkRecordKey(position, dimension, DATA_2D_TAG)

        fun finalizedState(position: ChunkPos, dimension: Int = OVERWORLD_DIMENSION): BedrockChunkRecordKey =
            BedrockChunkRecordKey(position, dimension, FINALIZED_STATE_TAG)

        fun subChunk(
            position: ChunkPos,
            subChunkY: Int,
            dimension: Int = OVERWORLD_DIMENSION,
        ): BedrockChunkRecordKey =
            BedrockChunkRecordKey(position, dimension, SUBCHUNK_TAG, byteArrayOf(subChunkY.toByte()))

        fun tagOffset(dimension: Int): Int = if (dimension == OVERWORLD_DIMENSION) 8 else 12

        fun chunkPrefixLength(dimension: Int): Int = tagOffset(dimension)

        fun subChunkY(key: ByteArray, dimension: Int = OVERWORLD_DIMENSION): Int? {
            val tagOffset = tagOffset(dimension)
            if (key.size <= tagOffset + 1 || !hasExpectedDimension(key, dimension) || key[tagOffset] != SUBCHUNK_TAG) {
                return null
            }
            return key[tagOffset + 1].toInt()
        }

        fun isData2d(key: ByteArray, dimension: Int = OVERWORLD_DIMENSION): Boolean {
            val tagOffset = tagOffset(dimension)
            return key.size > tagOffset &&
                key.size == tagOffset + 1 &&
                hasExpectedDimension(key, dimension) &&
                key[tagOffset] == DATA_2D_TAG
        }

        fun isVersion(key: ByteArray, dimension: Int = OVERWORLD_DIMENSION): Boolean {
            val tagOffset = tagOffset(dimension)
            return key.size > tagOffset &&
                key.size == tagOffset + 1 &&
                hasExpectedDimension(key, dimension) &&
                key[tagOffset] == VERSION_TAG
        }

        fun isFinalizedState(key: ByteArray, dimension: Int = OVERWORLD_DIMENSION): Boolean {
            val tagOffset = tagOffset(dimension)
            return key.size > tagOffset &&
                key.size == tagOffset + 1 &&
                hasExpectedDimension(key, dimension) &&
                key[tagOffset] == FINALIZED_STATE_TAG
        }

        private fun hasExpectedDimension(key: ByteArray, dimension: Int): Boolean {
            if (dimension == OVERWORLD_DIMENSION) {
                return true
            }
            if (key.size < DIMENSION_KEY_BYTES) {
                return false
            }
            val encodedDimension = (key[8].toInt() and 0xff) or
                ((key[9].toInt() and 0xff) shl 8) or
                ((key[10].toInt() and 0xff) shl 16) or
                ((key[11].toInt() and 0xff) shl 24)
            return encodedDimension == dimension
        }
    }
}
