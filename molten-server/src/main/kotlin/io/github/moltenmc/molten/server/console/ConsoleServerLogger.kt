package io.github.moltenmc.molten.server.console

class ConsoleServerLogger : ServerLogger {
    override fun info(message: String) {
        println("[INFO] $message")
    }

    override fun warn(message: String) {
        println("[WARN] $message")
    }

    override fun error(message: String, cause: Throwable?) {
        println("[ERROR] $message")
        cause?.printStackTrace(System.out)
    }
}
