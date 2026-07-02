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

    fun tickIngressAll(): Int =
        tickOpenEntries { entry ->
            entry.handler.tickIngress()
        }

    fun tickEgressAll(): Int =
        tickOpenEntries { entry ->
            entry.handler.tickEgress(entry.context)
        }

    fun tickAll(): Int =
        tickEgressAll()

    private fun tickOpenEntries(tickEntry: (Entry) -> Int): Int =
        entries.sumOf { entry ->
            if (entry.context.channel().isOpen) {
                tickEntry(entry)
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
