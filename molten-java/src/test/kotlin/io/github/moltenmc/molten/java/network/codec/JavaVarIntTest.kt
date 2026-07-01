package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.BufferAllocator
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JavaVarIntTest {
    @Test
    fun encodesKnownValues() {
        assertContentEquals(byteArrayOf(0x00), JavaVarInt.encode(0))
        assertContentEquals(byteArrayOf(0x01), JavaVarInt.encode(1))
        assertContentEquals(byteArrayOf(0x7f), JavaVarInt.encode(127))
        assertContentEquals(byteArrayOf(0x80.toByte(), 0x01), JavaVarInt.encode(128))
        assertContentEquals(byteArrayOf(0xff.toByte(), 0xff.toByte(), 0x7f), JavaVarInt.encode(2_097_151))
    }

    @Test
    fun decodesRoundTripValues() {
        val values = listOf(0, 1, 2, 127, 128, 255, 2_097_151, Int.MAX_VALUE, -1)

        BufferAllocator.onHeapUnpooled().use { allocator ->
            values.forEach { value ->
                val buffer = allocator.copyOf(JavaVarInt.encode(value))

                assertEquals(value, JavaVarInt.readOrNull(buffer))
                assertEquals(0, buffer.readableBytes())

                buffer.close()
            }
        }
    }

    @Test
    fun incompleteVarIntReturnsNullAndResetsReaderOffset() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = allocator.copyOf(byteArrayOf(0x80.toByte()))

            assertNull(JavaVarInt.readOrNull(buffer))
            assertEquals(0, buffer.readerOffset())

            buffer.close()
        }
    }

    @Test
    fun rejectsOversizedVarInt() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = allocator.copyOf(
                byteArrayOf(
                    0x80.toByte(),
                    0x80.toByte(),
                    0x80.toByte(),
                    0x80.toByte(),
                    0x80.toByte(),
                    0x01,
                ),
            )

            assertFailsWith<IllegalArgumentException> {
                JavaVarInt.readOrNull(buffer)
            }

            buffer.close()
        }
    }
}
