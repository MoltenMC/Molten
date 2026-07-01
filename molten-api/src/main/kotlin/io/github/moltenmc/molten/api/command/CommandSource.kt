package io.github.moltenmc.molten.api.command

interface CommandSource {
    val name: String

    val sourceType: CommandSourceType

    fun sendMessage(message: String)
}
