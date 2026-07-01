package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.codec.JavaStatusResponsePacketCodec
import io.github.moltenmc.molten.java.network.packet.StatusRequestPacket
import io.github.moltenmc.molten.java.network.packet.StatusResponsePacket
import io.github.moltenmc.molten.java.status.DefaultJavaStatusResponseProvider
import io.github.moltenmc.molten.java.status.JavaStatusJson
import io.github.moltenmc.molten.java.status.JavaStatusResponseProvider
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaStatusRequestHandler(
    private val statusProvider: JavaStatusResponseProvider = DefaultJavaStatusResponseProvider(),
) : ChannelHandlerAdapter() {
    fun responseFor(packet: StatusRequestPacket): StatusResponsePacket =
        StatusResponsePacket(
            packetId = JavaStatusResponsePacketCodec.PACKET_ID,
            json = JavaStatusJson.encode(statusProvider.currentStatus()),
        )

    fun handle(packet: Any): Any =
        if (packet is StatusRequestPacket) {
            responseFor(packet)
        } else {
            packet
        }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val handled = handle(msg)
        if (handled is StatusResponsePacket) {
            ctx.write(handled)
            ctx.flush()
        } else {
            ctx.fireChannelRead(handled)
        }
    }
}
