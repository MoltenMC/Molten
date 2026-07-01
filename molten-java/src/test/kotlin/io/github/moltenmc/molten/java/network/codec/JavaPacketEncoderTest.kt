package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistries
import io.github.moltenmc.molten.java.network.session.JavaProtocolStateHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.buffer.BufferAllocator
import io.netty5.handler.codec.EncoderException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class JavaPacketEncoderTest {
    @Test
    fun encodesRegisteredStatusResponsePacket() {
        val encoder = JavaPacketEncoder(
            stateHolder = JavaProtocolStateHolder(JavaProtocolState.STATUS),
        )

        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = encoder.encodePacket(
                StatusResponsePacket(packetId = 0x00, json = "{}"),
                allocator,
            )
            val bytes = ByteArray(buffer.readableBytes())
            buffer.readBytes(bytes, 0, bytes.size)

            assertContentEquals(byteArrayOf(0x00, 0x02, '{'.code.toByte(), '}'.code.toByte()), bytes)

            buffer.close()
        }
    }

    @Test
    fun rejectsUnknownPacketId() {
        val encoder = JavaPacketEncoder(
            stateHolder = JavaProtocolStateHolder(JavaProtocolState.STATUS),
        )

        BufferAllocator.onHeapUnpooled().use { allocator ->
            assertFailsWith<EncoderException> {
                encoder.encodePacket(TestPacket(packetId = 0x7f), allocator)
            }
        }
    }

    @Test
    fun rejectsPacketRegisteredForDifferentState() {
        val encoder = JavaPacketEncoder(
            registry = JavaPacketRegistries.protocol776(),
            stateHolder = JavaProtocolStateHolder(JavaProtocolState.HANDSHAKE),
        )

        BufferAllocator.onHeapUnpooled().use { allocator ->
            assertFailsWith<EncoderException> {
                encoder.encodePacket(StatusResponsePacket(packetId = 0x00, json = "{}"), allocator)
            }
        }
    }

    private data class TestPacket(
        override val packetId: Int,
    ) : JavaPacket
}
