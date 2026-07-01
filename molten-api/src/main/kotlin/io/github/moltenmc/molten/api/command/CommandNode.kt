package io.github.moltenmc.molten.api.command

data class CommandNode(
    val name: String,
    val description: String = "",
    val permission: String? = null,
    val arguments: List<CommandArgument> = emptyList(),
    val children: List<CommandNode> = emptyList(),
)
