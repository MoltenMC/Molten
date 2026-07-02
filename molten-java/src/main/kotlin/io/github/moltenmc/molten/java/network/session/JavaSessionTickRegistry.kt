package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.java.network.handler.JavaSessionTickHandler
import io.netty5.channel.ChannelHandlerContext
import java.util.concurrent.CopyOnWriteArraySet

class JavaSessionTickRegistry {
    private val entries = CopyOnWriteArraySet<Entry>()

    val size: Int
        get() = entries.size

    fun register(handler: JavaSessionTickHandler, context: ChannelHandlerContext): Registration {
        val entry = Entry(handler, context)
        entries += entry
        return Registration {
            entries -= entry
        }
    }

    fun tickAll(): Int =
        entries.sumOf { entry ->
            if (entry.context.channel().isOpen) {
                entry.handler.tick(entry.context)
            } else {
                entries -= entry
                0
            }
        }

    fun interface Registration {
        fun unregister()
    }

    private data class Entry(
        val handler: JavaSessionTickHandler,
        val context: ChannelHandlerContext,
    )
}
