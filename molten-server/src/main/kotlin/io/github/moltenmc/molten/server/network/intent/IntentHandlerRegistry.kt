package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.network.intent.ServerIntent

/**
 * Registry for intent handlers, mapping intent types to their handlers.
 */
class IntentHandlerRegistry {
    private val handlers = mutableMapOf<Class<out ServerIntent>, IntentHandler<*>>()

    /**
     * Registers a handler for a specific intent type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ServerIntent> register(intentType: Class<T>, handler: IntentHandler<T>) {
        handlers[intentType] = handler
    }

    /**
     * Gets the handler for a specific intent type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ServerIntent> getHandler(intentType: Class<T>): IntentHandler<T>? {
        return handlers[intentType] as? IntentHandler<T>
    }

    /**
     * Handles an intent using the registered handler.
     * Returns true if a handler was found and executed, false otherwise.
     */
    @Suppress("UNCHECKED_CAST")
    fun handle(intent: ServerIntent, context: IntentHandlerContext): Boolean {
        val handler = handlers[intent.javaClass] as? IntentHandler<ServerIntent>
        if (handler != null) {
            handler.handle(intent, context)
            return true
        }
        return false
    }
}
