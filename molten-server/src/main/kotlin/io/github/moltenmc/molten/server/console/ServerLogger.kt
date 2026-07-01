package io.github.moltenmc.molten.server.console

interface ServerLogger {
    fun info(message: String)

    fun warn(message: String)

    fun error(message: String, cause: Throwable? = null)
}
