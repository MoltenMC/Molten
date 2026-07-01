package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistries
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistry
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistryEntry
import io.github.moltenmc.molten.java.network.session.JavaProtocolStateHolder
import io.netty5.buffer.Buffer
import io.netty5.buffer.BufferAllocator
import io.netty5.channel.ChannelHandlerContext
import io.netty5.handler.codec.EncoderException
import io.netty5.handler.codec.MessageToMessageEncoder

class JavaPacketEncoder(
    private val registry: JavaPacketRegistry = JavaPacketRegistries.protocol776(),
    private val stateHolder: JavaProtocolStateHolder = JavaProtocolStateHolder(),
    private val protocolVersion: Int = JavaEditionProtocol.PROTOCOL_VERSION,
) : MessageToMessageEncoder<JavaPacket>(JavaPacket::class.java) {
    fun encodePacket(packet: JavaPacket, allocator: BufferAllocator): Buffer {
        val entry = registry.find(
            state = stateHolder.state,
            direction = PacketDirection.CLIENTBOUND,
            packetId = packet.packetId,
        ) ?: throw EncoderException("Unknown Java packet id ${packet.packetId} in state ${stateHolder.state}.")

        val bytes = entry.encode(
            packet,
            ProtocolContext(
                protocolVersion = protocolVersion,
                direction = PacketDirection.CLIENTBOUND,
            ),
        )
        return allocator.copyOf(bytes)
    }

    override fun encode(ctx: ChannelHandlerContext, msg: JavaPacket, out: MutableList<Any>) {
        out += encodePacket(msg, ctx.bufferAllocator())
    }

    @Suppress("UNCHECKED_CAST")
    private fun JavaPacketRegistryEntry<*>.encode(
        packet: JavaPacket,
        protocolContext: ProtocolContext,
    ): ByteArray =
        (codec as JavaPacketCodec<JavaPacket>).encode(packet, protocolContext)
}
