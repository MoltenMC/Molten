package io.github.moltenmc.molten.translator.command

data class InternalCommandNode(
    val name: String,
    val description: String,
    val permission: String? = null,
    val arguments: List<String> = emptyList(),
    val children: List<InternalCommandNode> = emptyList(),
    val suggestionProvider: String? = null,
)
