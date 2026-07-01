package io.github.moltenmc.molten.translator.command

interface CommandTranslator {
    fun translateCommandTree(commandTree: Any): Any
}
