package io.github.moltenmc.molten.common.network.intent

fun interface ServerIntentSink {
    fun accept(intent: ServerIntent)

    fun acceptAll(intents: Iterable<ServerIntent>) {
        intents.forEach(::accept)
    }

    companion object {
        val Noop: ServerIntentSink = ServerIntentSink { }
    }
}
