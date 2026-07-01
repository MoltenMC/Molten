package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.network.session.JavaSessionProfile
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaLoginStartHandler(
    private val sessionHolder: JavaSessionHolder,
) : ChannelHandlerAdapter() {
    fun loginSuccessFor(packet: LoginStartPacket): LoginSuccessPacket =
        LoginSuccessPacket(
            packetId = 0x02,
            uuid = packet.playerUuid,
            username = packet.name,
        )

    fun handle(packet: Any): Any =
        if (packet is LoginStartPacket) {
            completeLogin(packet)
        } else {
            packet
        }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is LoginStartPacket) {
            val response = loginSuccessFor(msg)
            sessionHolder.profile = profileFor(msg)
            ctx.channel().writeAndFlush(response)
            sessionHolder.state = JavaProtocolState.CONFIGURATION
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    private fun completeLogin(packet: LoginStartPacket): LoginSuccessPacket {
        sessionHolder.profile = profileFor(packet)
        sessionHolder.state = JavaProtocolState.CONFIGURATION
        return loginSuccessFor(packet)
    }

    private fun profileFor(packet: LoginStartPacket): JavaSessionProfile =
        JavaSessionProfile(
            uuid = packet.playerUuid,
            name = packet.name,
        )
}
