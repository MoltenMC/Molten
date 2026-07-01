package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.BufferAllocator
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JavaPacketFrameTest {
    @Test
    fun readsCompletePayloadFrame() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(byteArrayOf(0x03, 0x01, 0x02, 0x03))
            val payload = JavaPacketFrame.tryReadPayload(input)

            val bytes = ByteArray(payload!!.readableBytes())
            payload.readBytes(bytes, 0, bytes.size)

            assertContentEquals(byteArrayOf(0x01, 0x02, 0x03), bytes)
            assertEquals(0, input.readableBytes())

            payload.close()
            input.close()
        }
    }

    @Test
    fun incompletePayloadReturnsNullAndResetsReaderOffset() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(byteArrayOf(0x03, 0x01, 0x02))

            assertNull(JavaPacketFrame.tryReadPayload(input))
            assertEquals(0, input.readerOffset())

            input.close()
        }
    }

    @Test
    fun rejectsOversizedPayload() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(byteArrayOf(0x04, 0x01, 0x02, 0x03, 0x04))

            assertFailsWith<IllegalArgumentException> {
                JavaPacketFrame.tryReadPayload(input, maxFrameBytes = 3)
            }
            assertEquals(0, input.readerOffset())

            input.close()
        }
    }

    @Test
    fun writesLengthPrefixedPayload() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val payload = allocator.copyOf(byteArrayOf(0x01, 0x02, 0x03))
            val output = allocator.allocate(4)

            JavaPacketFrame.writePayload(payload, output)
            val bytes = ByteArray(output.readableBytes())
            output.readBytes(bytes, 0, bytes.size)

            assertContentEquals(byteArrayOf(0x03, 0x01, 0x02, 0x03), bytes)

            output.close()
            payload.close()
        }
    }
}
