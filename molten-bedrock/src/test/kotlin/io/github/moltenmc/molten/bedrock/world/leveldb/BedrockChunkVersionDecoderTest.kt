package io.github.moltenmc.molten.bedrock.world.leveldb

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BedrockChunkVersionDecoderTest {
    @Test
    fun decodesEmptyPayloadAsNull() {
        assertEquals(null, BedrockChunkVersionDecoder.decode(ByteArray(0)))
    }

    @Test
    fun decodesSingleBytePayload() {
        assertEquals(40, BedrockChunkVersionDecoder.decode(byteArrayOf(40)))
    }

    @Test
    fun decodesLittleEndianIntPayload() {
        assertEquals(300, BedrockChunkVersionDecoder.decode(byteArrayOf(44, 1, 0, 0)))
    }

    @Test
    fun encodesSingleByteVersion() {
        assertContentEquals(byteArrayOf(40), BedrockChunkVersionDecoder.encode(40))
    }

    @Test
    fun rejectsVersionsOutsideByteRange() {
        assertFailsWith<IllegalArgumentException> {
            BedrockChunkVersionDecoder.encode(300)
        }
    }
}
