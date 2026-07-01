package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.PlayDisconnectPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaPlayDisconnectPacketCodec : JavaPacketCodec<PlayDisconnectPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): PlayDisconnectPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing play disconnect packet id.")
                require(packetId == PACKET_ID) { "Unexpected play disconnect packet id: $packetId." }
                val reasonJson = JavaStringCodec.read(input, MAX_REASON_CHARACTERS)
                require(input.readableBytes() == 0) { "Play disconnect packet contains trailing bytes." }
                return PlayDisconnectPacket(packetId, reasonJson)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: PlayDisconnectPacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) {
            "Unexpected play disconnect packet id: ${packet.packetId}."
        }
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                JavaStringCodec.write(packet.reasonJson, output, MAX_REASON_CHARACTERS)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: PlayDisconnectPacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid play disconnect packet id.", disconnect = true)
        }
        if (packet.reasonJson.isBlank()) {
            return PacketValidationResult.Rejected("Play disconnect reason is blank.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: PlayDisconnectPacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            JavaVarInt.encodedSize(packet.reasonJson.length * 4) +
            packet.reasonJson.length * 4

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x1d
        const val MAX_REASON_CHARACTERS: Int = 32767
    }
}
