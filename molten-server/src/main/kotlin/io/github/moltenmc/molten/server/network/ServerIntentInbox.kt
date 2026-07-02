package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.network.intent.ServerIntentSink
import java.util.concurrent.ConcurrentLinkedQueue

class ServerIntentInbox : ServerIntentSink {
    private val intents = ConcurrentLinkedQueue<ServerIntent>()

    val size: Int
        get() = intents.size

    override fun accept(intent: ServerIntent) {
        intents += intent
    }

    fun drain(): List<ServerIntent> {
        val drained = mutableListOf<ServerIntent>()
        while (true) {
            drained += intents.poll() ?: break
        }
        return drained
    }
}
