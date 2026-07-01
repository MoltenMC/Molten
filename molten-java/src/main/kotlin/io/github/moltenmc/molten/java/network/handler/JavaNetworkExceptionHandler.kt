package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaNetworkExceptionHandler(
    private val sessionHolder: JavaSessionHolder,
) : ChannelHandlerAdapter() {
    fun loginDisconnectFor(cause: Throwable): LoginDisconnectPacket =
        JavaDisconnectPackets.login(reasonFor(cause))

    override fun channelExceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (sessionHolder.state == JavaProtocolState.LOGIN) {
            ctx.channel().writeAndFlush(loginDisconnectFor(cause)).addListener {
                closeAsDisconnected(ctx)
            }
        } else {
            closeAsDisconnected(ctx)
        }
    }

    private fun closeAsDisconnected(ctx: ChannelHandlerContext) {
        sessionHolder.state = JavaProtocolState.DISCONNECTED
        ctx.channel().close()
    }

    private fun reasonFor(cause: Throwable): String =
        cause.message
            ?.takeIf { it.isNotBlank() }
            ?: "Disconnected during login."
}
