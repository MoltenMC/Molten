package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator
import java.util.UUID

class JavaLoginStartPacketCodec : JavaPacketCodec<LoginStartPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): LoginStartPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing login start packet id.")
                require(packetId == PACKET_ID) { "Unexpected login start packet id: $packetId." }

                val name = JavaStringCodec.read(input, MAX_USERNAME_CHARACTERS)
                require(input.readableBytes() >= UUID_BYTES) { "Missing login start player UUID." }
                val playerUuid = UUID(input.readLong(), input.readLong())
                require(input.readableBytes() == 0) { "Login start packet contains trailing bytes." }

                return LoginStartPacket(
                    packetId = packetId,
                    name = name,
                    playerUuid = playerUuid,
                )
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: LoginStartPacket, protocolContext: ProtocolContext): ByteArray {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                JavaStringCodec.write(packet.name, output, MAX_USERNAME_CHARACTERS)
                output.writeLong(packet.playerUuid.mostSignificantBits)
                output.writeLong(packet.playerUuid.leastSignificantBits)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: LoginStartPacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid login start packet id.", disconnect = true)
        }
        if (packet.name.isBlank()) {
            return PacketValidationResult.Rejected("Login username is blank.", disconnect = true)
        }
        if (packet.name.length > MAX_USERNAME_CHARACTERS) {
            return PacketValidationResult.Rejected("Login username is too long.", disconnect = true)
        }
        if (!USERNAME_PATTERN.matches(packet.name)) {
            return PacketValidationResult.Rejected("Login username contains invalid characters.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: LoginStartPacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            JavaVarInt.encodedSize(packet.name.length * 4) +
            packet.name.length * 4 +
            UUID_BYTES

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x00
        const val MAX_USERNAME_CHARACTERS: Int = 16
        private const val UUID_BYTES: Int = 16
        private val USERNAME_PATTERN = Regex("[A-Za-z0-9_]{1,16}")
    }
}
