package io.github.moltenmc.molten.java.network.handler

import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaSessionTickHandler(
    private val outboundFlushHandler: JavaOutboundFlushHandler,
) : ChannelHandlerAdapter() {
    fun tick(ctx: ChannelHandlerContext): Int =
        outboundFlushHandler.flushQueuedMessages(ctx)
}
