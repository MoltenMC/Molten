package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.AcknowledgeFinishConfigurationPacket
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaConfigurationFinishHandler(
    private val sessionHolder: JavaSessionHolder,
    private val playStartHandler: JavaPlayStartHandler = JavaPlayStartHandler(),
) : ChannelHandlerAdapter() {
    data class ConfigurationFinishResult(
        val playPackets: List<JavaPacket>,
    )

    fun handle(packet: Any): Any =
        if (packet is AcknowledgeFinishConfigurationPacket) {
            enterPlay()
        } else {
            packet
        }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is AcknowledgeFinishConfigurationPacket) {
            enterPlay().playPackets.forEach { packet ->
                ctx.channel().write(packet)
            }
            ctx.channel().flush()
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    private fun enterPlay(): ConfigurationFinishResult {
        sessionHolder.state = JavaProtocolState.PLAY
        return ConfigurationFinishResult(
            playPackets = playStartHandler.playPacketsFor(sessionHolder),
        )
    }
}
