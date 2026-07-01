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
        val reason = reasonFor(cause)
        val disconnectPacket = when (sessionHolder.state) {
            JavaProtocolState.LOGIN -> JavaDisconnectPackets.login(reason)
            JavaProtocolState.CONFIGURATION -> JavaDisconnectPackets.configuration(reason)
            JavaProtocolState.PLAY -> JavaDisconnectPackets.play(reason)
            JavaProtocolState.HANDSHAKE,
            JavaProtocolState.STATUS,
            JavaProtocolState.DISCONNECTED,
            -> null
        }

        if (disconnectPacket == null) {
            closeAsDisconnected(ctx)
        } else {
            ctx.channel().writeAndFlush(disconnectPacket).addListener {
                closeAsDisconnected(ctx)
            }
        }
    }

    private fun closeAsDisconnected(ctx: ChannelHandlerContext) {
        sessionHolder.state = JavaProtocolState.DISCONNECTED
        ctx.channel().close()
    }

    private fun reasonFor(cause: Throwable): String =
        cause.message
            ?.takeIf { it.isNotBlank() }
            ?: "Disconnected."
}
