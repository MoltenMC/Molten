package io.github.moltenmc.molten.api.command

data class CommandArgument(
    val name: String,
    val type: CommandArgumentType,
    val optional: Boolean = false,
)
