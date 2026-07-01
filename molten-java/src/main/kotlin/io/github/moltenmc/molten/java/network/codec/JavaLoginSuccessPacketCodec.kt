package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import io.github.moltenmc.molten.java.network.packet.LoginSuccessProperty
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator
import java.util.UUID

class JavaLoginSuccessPacketCodec : JavaPacketCodec<LoginSuccessPacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): LoginSuccessPacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing login success packet id.")
                require(packetId == PACKET_ID) { "Unexpected login success packet id: $packetId." }

                val uuid = UUID(input.readLong(), input.readLong())
                val username = JavaStringCodec.read(input, MAX_USERNAME_CHARACTERS)
                val propertyCount = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing login success property count.")
                require(propertyCount >= 0) { "Login success property count must not be negative." }
                val properties = List(propertyCount) {
                    LoginSuccessProperty(
                        name = JavaStringCodec.read(input, MAX_PROPERTY_NAME_CHARACTERS),
                        value = JavaStringCodec.read(input, MAX_PROPERTY_VALUE_CHARACTERS),
                        signature = if (input.readBoolean()) {
                            JavaStringCodec.read(input, MAX_PROPERTY_SIGNATURE_CHARACTERS)
                        } else {
                            null
                        },
                    )
                }
                require(input.readableBytes() == 0) { "Login success packet contains trailing bytes." }

                return LoginSuccessPacket(packetId, uuid, username, properties)
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: LoginSuccessPacket, protocolContext: ProtocolContext): ByteArray {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                output.writeLong(packet.uuid.mostSignificantBits)
                output.writeLong(packet.uuid.leastSignificantBits)
                JavaStringCodec.write(packet.username, output, MAX_USERNAME_CHARACTERS)
                JavaVarInt.write(packet.properties.size, output)
                packet.properties.forEach { property ->
                    JavaStringCodec.write(property.name, output, MAX_PROPERTY_NAME_CHARACTERS)
                    JavaStringCodec.write(property.value, output, MAX_PROPERTY_VALUE_CHARACTERS)
                    output.writeBoolean(property.signature != null)
                    property.signature?.let {
                        JavaStringCodec.write(it, output, MAX_PROPERTY_SIGNATURE_CHARACTERS)
                    }
                }
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: LoginSuccessPacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid login success packet id.", disconnect = true)
        }
        if (packet.username.isBlank() || packet.username.length > MAX_USERNAME_CHARACTERS) {
            return PacketValidationResult.Rejected("Invalid login success username.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: LoginSuccessPacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            UUID_BYTES +
            JavaVarInt.encodedSize(packet.username.length * 4) +
            packet.username.length * 4 +
            JavaVarInt.encodedSize(packet.properties.size) +
            packet.properties.sumOf { property ->
                JavaVarInt.encodedSize(property.name.length * 4) +
                    property.name.length * 4 +
                    JavaVarInt.encodedSize(property.value.length * 4) +
                    property.value.length * 4 +
                    1 +
                    (property.signature?.let { JavaVarInt.encodedSize(it.length * 4) + it.length * 4 } ?: 0)
            }

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x02
        const val MAX_USERNAME_CHARACTERS: Int = 16
        private const val UUID_BYTES: Int = 16
        private const val MAX_PROPERTY_NAME_CHARACTERS = 32767
        private const val MAX_PROPERTY_VALUE_CHARACTERS = 32767
        private const val MAX_PROPERTY_SIGNATURE_CHARACTERS = 32767
    }
}
