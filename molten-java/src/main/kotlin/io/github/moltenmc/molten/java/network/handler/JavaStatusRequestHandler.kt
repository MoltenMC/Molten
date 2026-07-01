package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.codec.JavaStatusResponsePacketCodec
import io.github.moltenmc.molten.java.network.packet.StatusPingPacket
import io.github.moltenmc.molten.java.network.packet.StatusPongPacket
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

    fun pongFor(packet: StatusPingPacket): StatusPongPacket =
        StatusPongPacket(
            packetId = packet.packetId,
            payload = packet.payload,
        )

    fun handle(packet: Any): Any =
        when (packet) {
            is StatusRequestPacket -> responseFor(packet)
            is StatusPingPacket -> pongFor(packet)
            else -> packet
        }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val handled = handle(msg)
        if (handled is StatusResponsePacket || handled is StatusPongPacket) {
            ctx.write(handled)
            ctx.flush()
        } else {
            ctx.fireChannelRead(handled)
        }
    }
}
