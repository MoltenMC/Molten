package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.network.intent.ServerIntent
import java.util.ArrayDeque

class JavaInboundIntentQueue {
    private val intents = ArrayDeque<ServerIntent>()

    val size: Int
        get() = intents.size

    fun enqueue(intent: ServerIntent) {
        intents.addLast(intent)
    }

    fun drain(): List<ServerIntent> {
        val drained = ArrayList<ServerIntent>(intents.size)
        while (intents.isNotEmpty()) {
            drained += intents.removeFirst()
        }
        return drained
    }
}
