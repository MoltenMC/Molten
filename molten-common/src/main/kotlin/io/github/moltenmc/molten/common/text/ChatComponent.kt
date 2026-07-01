package io.github.moltenmc.molten.common.text

sealed interface ChatComponent {
    val plainText: String
}

data class TextComponent(
    override val plainText: String,
) : ChatComponent {
    init {
        require(plainText.isNotEmpty()) { "Text component must not be empty." }
    }
}
