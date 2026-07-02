package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.network.intent.ServerIntentSink
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaSessionTickHandler(
    private val outboundFlushHandler: JavaOutboundFlushHandler,
    private val sessionHolder: JavaSessionHolder,
    private val intentSink: ServerIntentSink = ServerIntentSink.Noop,
) : ChannelHandlerAdapter() {
    fun tick(ctx: ChannelHandlerContext): Int {
        drainInboundIntents()
        return outboundFlushHandler.flushQueuedMessages(ctx)
    }

    fun drainInboundIntents(): Int {
        val intents = sessionHolder.inboundIntentQueue.drain()
        intentSink.acceptAll(intents)
        return intents.size
    }
}
