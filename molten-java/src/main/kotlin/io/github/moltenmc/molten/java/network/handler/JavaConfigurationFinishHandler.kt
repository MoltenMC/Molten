package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.AcknowledgeFinishConfigurationPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaConfigurationFinishHandler(
    private val sessionHolder: JavaSessionHolder,
) : ChannelHandlerAdapter() {
    fun handle(packet: Any): Any {
        if (packet is AcknowledgeFinishConfigurationPacket) {
            sessionHolder.state = JavaProtocolState.PLAY
        }
        return packet
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        ctx.fireChannelRead(handle(msg))
    }
}
