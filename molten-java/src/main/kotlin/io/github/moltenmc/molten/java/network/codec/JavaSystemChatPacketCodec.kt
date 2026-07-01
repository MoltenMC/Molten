package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaSystemChatPacketCodec : JavaPacketCodec<SystemChatPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): SystemChatPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing system chat packet id.")
                require(packetId == PACKET_ID) { "Unexpected system chat packet id: $packetId." }
                val contentJson = JavaStringCodec.read(input, MAX_CONTENT_CHARACTERS)
                require(input.readableBytes() >= 1) { "Missing system chat overlay flag." }
                val overlay = input.readBoolean()
                require(input.readableBytes() == 0) { "System chat packet contains trailing bytes." }
                return SystemChatPacket(packetId, contentJson, overlay)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: SystemChatPacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) { "Unexpected system chat packet id: ${packet.packetId}." }
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                JavaStringCodec.write(packet.contentJson, output, MAX_CONTENT_CHARACTERS)
                output.writeBoolean(packet.overlay)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: SystemChatPacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid system chat packet id.", disconnect = true)
        }
        if (packet.contentJson.isBlank()) {
            return PacketValidationResult.Rejected("System chat content is blank.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: SystemChatPacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            JavaVarInt.encodedSize(packet.contentJson.length * 4) +
            packet.contentJson.length * 4 +
            1

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x72
        const val MAX_CONTENT_CHARACTERS: Int = 32767
    }
}
