package io.github.moltenmc.molten.api.command

interface CommandRegistry {
    fun register(command: Command)
}
