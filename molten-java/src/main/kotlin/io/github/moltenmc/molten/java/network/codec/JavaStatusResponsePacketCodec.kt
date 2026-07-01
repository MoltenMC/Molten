package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaStatusResponsePacketCodec : JavaPacketCodec<StatusResponsePacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): StatusResponsePacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing status response packet id.")
                require(packetId == PACKET_ID) { "Unexpected status response packet id: $packetId." }
                val json = JavaStringCodec.read(input, MAX_JSON_CHARACTERS)
                require(input.readableBytes() == 0) { "Status response packet contains trailing bytes." }
                return StatusResponsePacket(packetId, json)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: StatusResponsePacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) { "Unexpected status response packet id: ${packet.packetId}." }
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                JavaStringCodec.write(packet.json, output, MAX_JSON_CHARACTERS)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: StatusResponsePacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid status response packet id.", disconnect = true)
        }
        if (packet.json.isBlank()) {
            return PacketValidationResult.Rejected("Status response JSON is blank.", disconnect = true)
        }
        if (packet.json.length > MAX_JSON_CHARACTERS) {
            return PacketValidationResult.Rejected("Status response JSON is too long.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: StatusResponsePacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            JavaVarInt.encodedSize(packet.json.length * 4) +
            packet.json.length * 4

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x00
        const val MAX_JSON_CHARACTERS: Int = 32767
    }
}
