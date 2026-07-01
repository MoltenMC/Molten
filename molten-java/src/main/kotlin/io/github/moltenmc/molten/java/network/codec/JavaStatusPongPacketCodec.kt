package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.StatusPongPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaStatusPongPacketCodec : JavaPacketCodec<StatusPongPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): StatusPongPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing status pong packet id.")
                require(packetId == PACKET_ID) { "Unexpected status pong packet id: $packetId." }
                require(input.readableBytes() >= Long.SIZE_BYTES) { "Missing status pong payload." }
                val payload = input.readLong()
                require(input.readableBytes() == 0) { "Status pong packet contains trailing bytes." }
                return StatusPongPacket(packetId, payload)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: StatusPongPacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) { "Unexpected status pong packet id: ${packet.packetId}." }
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(JavaVarInt.encodedSize(packet.packetId) + Long.SIZE_BYTES)
            try {
                JavaVarInt.write(packet.packetId, output)
                output.writeLong(packet.payload)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: StatusPongPacket, sessionContext: SessionContext): PacketValidationResult =
        if (packet.packetId == PACKET_ID) {
            PacketValidationResult.Accepted
        } else {
            PacketValidationResult.Rejected("Invalid status pong packet id.", disconnect = true)
        }

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x01
    }
}
