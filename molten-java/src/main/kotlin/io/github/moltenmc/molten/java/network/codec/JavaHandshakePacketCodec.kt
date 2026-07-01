package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.network.packet.HandshakeNextState
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator

class JavaHandshakePacketCodec : JavaPacketCodec<HandshakePacket> {
    override fun decode(buffer: ByteArray, protocolContext: ProtocolContext): HandshakePacket {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val input = allocator.copyOf(buffer)
            try {
                val packetId = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing handshake packet id.")
                require(packetId == PACKET_ID) { "Unexpected handshake packet id: $packetId." }

                val protocolVersion = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing handshake protocol version.")
                val serverAddress = JavaStringCodec.read(input, MAX_SERVER_ADDRESS_CHARACTERS)
                val serverPort = input.readUnsignedShort()
                val nextState = JavaVarInt.readOrNull(input)
                    ?: throw IllegalArgumentException("Missing handshake next state.")
                require(input.readableBytes() == 0) { "Handshake packet contains trailing bytes." }

                return HandshakePacket(
                    packetId = packetId,
                    protocolVersion = protocolVersion,
                    serverAddress = serverAddress,
                    serverPort = serverPort,
                    nextState = HandshakeNextState.fromWireValue(nextState),
                )
            } finally {
                input.close()
            }
        }
    }

    override fun encode(packet: HandshakePacket, protocolContext: ProtocolContext): ByteArray {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val output = allocator.allocate(estimatedSize(packet))
            try {
                JavaVarInt.write(packet.packetId, output)
                JavaVarInt.write(packet.protocolVersion, output)
                JavaStringCodec.write(packet.serverAddress, output, MAX_SERVER_ADDRESS_CHARACTERS)
                output.writeUnsignedShort(packet.serverPort)
                JavaVarInt.write(packet.nextState.wireValue, output)
                return output.toByteArray()
            } finally {
                output.close()
            }
        }
    }

    override fun validate(packet: HandshakePacket, sessionContext: SessionContext): PacketValidationResult {
        if (packet.packetId != PACKET_ID) {
            return PacketValidationResult.Rejected("Invalid handshake packet id.", disconnect = true)
        }
        if (packet.serverAddress.isBlank()) {
            return PacketValidationResult.Rejected("Handshake server address is blank.", disconnect = true)
        }
        if (packet.serverAddress.length > MAX_SERVER_ADDRESS_CHARACTERS) {
            return PacketValidationResult.Rejected("Handshake server address is too long.", disconnect = true)
        }
        if (packet.serverPort !in PORT_RANGE) {
            return PacketValidationResult.Rejected("Handshake server port is out of range.", disconnect = true)
        }
        return PacketValidationResult.Accepted
    }

    private fun estimatedSize(packet: HandshakePacket): Int =
        JavaVarInt.encodedSize(packet.packetId) +
            JavaVarInt.encodedSize(packet.protocolVersion) +
            JavaVarInt.encodedSize(packet.serverAddress.length * 4) +
            packet.serverAddress.length * 4 +
            Short.SIZE_BYTES +
            JavaVarInt.encodedSize(packet.nextState.wireValue)

    private fun Buffer.toByteArray(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        const val PACKET_ID: Int = 0x00
        const val MAX_SERVER_ADDRESS_CHARACTERS: Int = 255
        private val PORT_RANGE = 0..65535
    }
}
