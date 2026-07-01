package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.BufferAllocator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaStringCodecTest {
    @Test
    fun writesAndReadsUtf8String() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(64)

            JavaStringCodec.write("localhost", output, maxCharacters = 255)

            assertEquals("localhost", JavaStringCodec.read(output, maxCharacters = 255))
            assertEquals(0, output.readableBytes())

            output.close()
        }
    }

    @Test
    fun rejectsStringLongerThanCharacterLimit() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(64)

            assertFailsWith<IllegalArgumentException> {
                JavaStringCodec.write("abcd", output, maxCharacters = 3)
            }

            output.close()
        }
    }

    @Test
    fun rejectsMalformedUtf8() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(byteArrayOf(0x01, 0xff.toByte()))

            assertFailsWith<Exception> {
                JavaStringCodec.read(input, maxCharacters = 255)
            }

            input.close()
        }
    }
}
