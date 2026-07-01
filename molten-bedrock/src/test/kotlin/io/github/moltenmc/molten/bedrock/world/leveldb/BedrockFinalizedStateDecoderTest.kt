package io.github.moltenmc.molten.bedrock.world.leveldb

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BedrockFinalizedStateDecoderTest {
    @Test
    fun decodesEmptyPayloadAsNull() {
        assertEquals(null, BedrockFinalizedStateDecoder.decode(ByteArray(0)))
    }

    @Test
    fun decodesSingleBytePayload() {
        assertEquals(5, BedrockFinalizedStateDecoder.decode(byteArrayOf(5)))
    }

    @Test
    fun decodesLittleEndianIntPayload() {
        assertEquals(7, BedrockFinalizedStateDecoder.decode(byteArrayOf(7, 0, 0, 0)))
    }

    @Test
    fun encodesLittleEndianIntPayload() {
        assertContentEquals(byteArrayOf(7, 0, 0, 0), BedrockFinalizedStateDecoder.encode(7))
    }
}
