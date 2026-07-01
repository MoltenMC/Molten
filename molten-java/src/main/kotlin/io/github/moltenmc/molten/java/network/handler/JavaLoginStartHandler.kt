package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.packet.LoginSuccessPacket
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.network.session.JavaSessionProfile
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaLoginStartHandler(
    private val sessionHolder: JavaSessionHolder,
    private val configurationStartHandler: JavaConfigurationStartHandler = JavaConfigurationStartHandler(),
) : ChannelHandlerAdapter() {
    data class LoginStartResult(
        val loginSuccess: LoginSuccessPacket,
        val configurationPackets: List<JavaPacket>,
    )

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
            ctx.channel().writeAndFlush(response).addListener { future ->
                if (future.isFailed) {
                    ctx.fireChannelExceptionCaught(future.cause())
                    return@addListener
                }

                sessionHolder.state = JavaProtocolState.CONFIGURATION
                configurationStartHandler.configurationPacketsFor(sessionHolder).forEach { packet ->
                    ctx.channel().write(packet)
                }
                ctx.channel().flush()
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    private fun completeLogin(packet: LoginStartPacket): LoginStartResult {
        sessionHolder.profile = profileFor(packet)
        sessionHolder.state = JavaProtocolState.CONFIGURATION
        return LoginStartResult(
            loginSuccess = loginSuccessFor(packet),
            configurationPackets = configurationStartHandler.configurationPacketsFor(sessionHolder),
        )
    }

    private fun profileFor(packet: LoginStartPacket): JavaSessionProfile =
        JavaSessionProfile(
            uuid = packet.playerUuid,
            name = packet.name,
        )
}
