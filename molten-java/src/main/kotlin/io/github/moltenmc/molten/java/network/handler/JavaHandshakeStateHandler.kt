package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.HandshakeNextState
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.network.session.JavaProtocolStateHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaHandshakeStateHandler(
    private val stateHolder: JavaProtocolStateHolder,
) : ChannelHandlerAdapter() {
    fun handle(packet: Any): Any {
        if (packet is HandshakePacket) {
            stateHolder.state = packet.nextState.toProtocolState()
        }
        return packet
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        ctx.fireChannelRead(handle(msg))
    }

    private fun HandshakeNextState.toProtocolState(): JavaProtocolState =
        when (this) {
            HandshakeNextState.STATUS -> JavaProtocolState.STATUS
            HandshakeNextState.LOGIN -> JavaProtocolState.LOGIN
        }
}
