package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.PacketValidationResult
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.common.network.SessionContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistries
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistry
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistryEntry
import io.github.moltenmc.molten.java.network.session.JavaProtocolStateHolder
import io.netty5.buffer.Buffer
import io.netty5.channel.ChannelHandlerContext
import io.netty5.handler.codec.DecoderException
import io.netty5.handler.codec.MessageToMessageDecoder
import java.net.SocketAddress
import java.util.UUID

class JavaPacketDecoder(
    private val registry: JavaPacketRegistry = JavaPacketRegistries.protocol776(),
    private val stateHolder: JavaProtocolStateHolder = JavaProtocolStateHolder(),
    private val protocolVersion: Int = JavaEditionProtocol.PROTOCOL_VERSION,
) : MessageToMessageDecoder<Buffer>(Buffer::class.java) {
    fun decodePayload(payload: Buffer, remoteAddress: SocketAddress? = null): JavaPacket {
        val startOffset = payload.readerOffset()
        val payloadBytes = payload.copyReadableBytes()
        val packetId = JavaVarInt.readOrNull(payload)
            ?: throw DecoderException("Missing Java packet id.")
        payload.readerOffset(startOffset)

        val entry = registry.find(
            state = stateHolder.state,
            direction = PacketDirection.SERVERBOUND,
            packetId = packetId,
        ) ?: throw DecoderException("Unknown Java packet id $packetId in state ${stateHolder.state}.")

        val packet = entry.decode(
            payloadBytes,
            ProtocolContext(
                protocolVersion = protocolVersion,
                direction = PacketDirection.SERVERBOUND,
            ),
        )
        entry.validate(packet, sessionContext(remoteAddress))
        return packet
    }

    override fun decode(ctx: ChannelHandlerContext, msg: Buffer) {
        ctx.fireChannelRead(decodePayload(msg, ctx.channel().remoteAddress()))
    }

    private fun Buffer.copyReadableBytes(): ByteArray {
        val bytes = ByteArray(readableBytes())
        copyInto(readerOffset(), bytes, 0, bytes.size)
        return bytes
    }

    @Suppress("UNCHECKED_CAST")
    private fun JavaPacketRegistryEntry<*>.decode(
        bytes: ByteArray,
        protocolContext: ProtocolContext,
    ): JavaPacket =
        (codec as JavaPacketCodec<JavaPacket>).decode(bytes, protocolContext)

    @Suppress("UNCHECKED_CAST")
    private fun JavaPacketRegistryEntry<*>.validate(
        packet: JavaPacket,
        sessionContext: SessionContext,
    ) {
        when (val result = (codec as JavaPacketCodec<JavaPacket>).validate(packet, sessionContext)) {
            PacketValidationResult.Accepted -> Unit
            is PacketValidationResult.Rejected -> throw DecoderException(result.reason)
        }
    }

    private fun sessionContext(remoteAddress: SocketAddress?): SessionContext =
        SessionContext(
            connectionId = EMPTY_CONNECTION_ID,
            remoteAddress = remoteAddress,
            protocolVersion = protocolVersion,
        )

    companion object {
        private val EMPTY_CONNECTION_ID = UUID(0L, 0L)
    }
}
