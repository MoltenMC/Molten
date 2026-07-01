package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaStatusRequestPacketCodec : JavaPacketCodec<StatusRequestPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): StatusRequestPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing status request packet id.")
                require(packetId == PACKET_ID) { "Unexpected status request packet id: $packetId." }
                require(input.readableBytes() == 0) { "Status request packet contains trailing bytes." }
                return StatusRequestPacket(packetId)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: StatusRequestPacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) { "Unexpected status request packet id: ${packet.packetId}." }
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(JavaVarInt.encodedSize(packet.packetId))
            try {
                JavaVarInt.write(packet.packetId, output)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: StatusRequestPacket, sessionContext: SessionContext): PacketValidationResult =
        if (packet.packetId == PACKET_ID) {
            PacketValidationResult.Accepted
        } else {
            PacketValidationResult.Rejected("Invalid status request packet id.", disconnect = true)
        }

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x00
    }
}
