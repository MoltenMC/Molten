package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.JavaPlayJoinPacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaPlayJoinPacketCodec : JavaPacketCodec<JavaPlayJoinPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): JavaPlayJoinPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing play join packet id.")
                require(packetId == PACKET_ID) { "Unexpected play join packet id: $packetId." }
                require(input.readableBytes() >= Int.SIZE_BYTES + 3) { "Missing play join fixed fields." }
                val entityId = input.readInt()
                val hardcore = input.readBoolean()
                val gameMode = input.readByte().toInt()
                val previousGameMode = input.readByte().toInt()
                val dimensionType = JavaStringCodec.read(input, MAX_RESOURCE_KEY_CHARACTERS)
                val worldName = JavaStringCodec.read(input, MAX_RESOURCE_KEY_CHARACTERS)
                require(input.readableBytes() >= Long.SIZE_BYTES) { "Missing play join hashed seed." }
                val hashedSeed = input.readLong()
                require(input.readableBytes() == 0) { "Play join packet contains trailing bytes." }
                return JavaPlayJoinPacket(
                    packetId = packetId,
                    entityId = entityId,
                    hardcore = hardcore,
                    gameMode = gameMode,
                    previousGameMode = previousGameMode,
                    dimensionType = dimensionType,
                    worldName = worldName,
                    hashedSeed = hashedSeed,
                )
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: JavaPlayJoinPacket, protocolContext: ProtocolContext): ByteArray {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                output.writeInt(packet.entityId)
                output.writeBoolean(packet.hardcore)
                output.writeByte(packet.gameMode.toByte())
                output.writeByte(packet.previousGameMode.toByte())
                JavaStringCodec.write(packet.dimensionType, output, MAX_RESOURCE_KEY_CHARACTERS)
                JavaStringCodec.write(packet.worldName, output, MAX_RESOURCE_KEY_CHARACTERS)
                output.writeLong(packet.hashedSeed)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: JavaPlayJoinPacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid play join packet id.", disconnect = true)
        }
        if (packet.dimensionType.isBlank() || packet.worldName.isBlank()) {
            return PacketValidationResult.Rejected("Play join dimension keys are required.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: JavaPlayJoinPacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            Int.SIZE_BYTES +
            1 +
            1 +
            1 +
            JavaVarInt.encodedSize(packet.dimensionType.length * 4) +
            packet.dimensionType.length * 4 +
            JavaVarInt.encodedSize(packet.worldName.length * 4) +
            packet.worldName.length * 4 +
            Long.SIZE_BYTES

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x2b
        const val MAX_RESOURCE_KEY_CHARACTERS: Int = 32767
    }
}
