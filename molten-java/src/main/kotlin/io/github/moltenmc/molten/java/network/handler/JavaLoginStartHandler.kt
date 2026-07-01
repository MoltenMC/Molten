package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.network.session.JavaSessionProfile
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaLoginStartHandler(
    private val sessionHolder: JavaSessionHolder,
) : ChannelHandlerAdapter() {
    fun handle(packet: Any): Any {
        if (packet is LoginStartPacket) {
            sessionHolder.profile = JavaSessionProfile(
                uuid = packet.playerUuid,
                name = packet.name,
            )
            sessionHolder.state = JavaProtocolState.CONFIGURATION
        }
        return packet
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        ctx.fireChannelRead(handle(msg))
    }
}
