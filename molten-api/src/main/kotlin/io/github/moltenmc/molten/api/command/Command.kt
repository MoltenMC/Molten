package io.github.moltenmc.molten.api.command

interface Command {
    val name: String

    val node: CommandNode
        get() = CommandNode(name)

    val executionPolicy: CommandExecutionPolicy
        get() = CommandExecutionPolicy.AUTHORITATIVE_REGION

    fun execute(source: CommandSource, arguments: List<String>)
}
