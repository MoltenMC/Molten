package io.github.moltenmc.molten.server.console

import io.github.moltenmc.molten.api.command.CommandSource
import io.github.moltenmc.molten.api.command.CommandSourceType

object ConsoleCommandSource : CommandSource {
    override val name: String = "Console"

    override val sourceType: CommandSourceType = CommandSourceType.CONSOLE

    override fun sendMessage(message: String) {
        println(message)
    }
}
