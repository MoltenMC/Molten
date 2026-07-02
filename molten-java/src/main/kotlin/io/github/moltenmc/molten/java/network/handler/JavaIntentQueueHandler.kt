package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.intent.JavaServerIntentAdapter
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.netty5.channel.ChannelHandlerAdapter
import io.netty5.channel.ChannelHandlerContext

class JavaIntentQueueHandler(
    private val sessionHolder: JavaSessionHolder,
    private val adapter: JavaServerIntentAdapter = JavaServerIntentAdapter(sessionHolder),
) : ChannelHandlerAdapter() {
    fun handle(packet: Any): Boolean {
        if (packet !is JavaPacket) {
            return false
        }
        val intent = adapter.toIntent(packet) ?: return false
        sessionHolder.inboundIntentQueue.enqueue(intent)
        return true
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (!handle(msg)) {
            ctx.fireChannelRead(msg)
        }
    }
}
