package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.FinishConfigurationPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaFinishConfigurationPacketCodec : JavaPacketCodec<FinishConfigurationPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): FinishConfigurationPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing finish configuration packet id.")
                require(packetId == PACKET_ID) { "Unexpected finish configuration packet id: $packetId." }
                require(input.readableBytes() == 0) { "Finish configuration packet contains trailing bytes." }
                return FinishConfigurationPacket(packetId)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: FinishConfigurationPacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) {
            "Unexpected finish configuration packet id: ${packet.packetId}."
        }
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

    override fun validate(
        packet: FinishConfigurationPacket,
        sessionContext: SessionContext,
    ): PacketValidationResult =
        if (packet.packetId == PACKET_ID) {
            PacketValidationResult.Accepted
        } else {
            PacketValidationResult.Rejected("Invalid finish configuration packet id.", disconnect = true)
        }

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x03
    }
}
