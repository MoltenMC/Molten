package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaOutboundFlushHandler(
    private val sessionHolder: JavaSessionHolder,
) : ChannelHandlerAdapter() {
    fun flushQueuedMessages(ctx: ChannelHandlerContext): Int {
        val packets = sessionHolder.outboundQueue.drainPackets(sessionHolder.state)
        packets.forEach(ctx.channel()::write)
        if (packets.isNotEmpty()) {
            ctx.channel().flush()
        }
        return packets.size
    }
}
