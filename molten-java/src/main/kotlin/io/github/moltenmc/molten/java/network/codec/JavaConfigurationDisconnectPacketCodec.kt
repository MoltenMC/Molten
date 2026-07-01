package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.ConfigurationDisconnectPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaConfigurationDisconnectPacketCodec : JavaPacketCodec<ConfigurationDisconnectPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): ConfigurationDisconnectPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing configuration disconnect packet id.")
                require(packetId == PACKET_ID) { "Unexpected configuration disconnect packet id: $packetId." }
                val reasonJson = JavaStringCodec.read(input, MAX_REASON_CHARACTERS)
                require(input.readableBytes() == 0) { "Configuration disconnect packet contains trailing bytes." }
                return ConfigurationDisconnectPacket(packetId, reasonJson)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: ConfigurationDisconnectPacket, protocolContext: ProtocolContext): ByteArray {
        require(packet.packetId == PACKET_ID) {
            "Unexpected configuration disconnect packet id: ${packet.packetId}."
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

    override fun validate(
        packet: ConfigurationDisconnectPacket,
        sessionContext: SessionContext,
    ): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid configuration disconnect packet id.", disconnect = true)
        }
        if (packet.reasonJson.isBlank()) {
            return PacketValidationResult.Rejected("Configuration disconnect reason is blank.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: ConfigurationDisconnectPacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            JavaVarInt.encodedSize(packet.reasonJson.length * 4) +
            packet.reasonJson.length * 4

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x02
        const val MAX_REASON_CHARACTERS: Int = 32767
    }
}
