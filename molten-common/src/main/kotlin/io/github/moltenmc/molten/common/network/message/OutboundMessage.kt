package io.github.moltenmc.molten.common.network.message

import io.github.moltenmc.molten.common.text.ChatComponent

sealed interface OutboundMessage {
    val content: ChatComponent
    val overlay: Boolean

    data class System(
        override val content: ChatComponent,
        override val overlay: Boolean = false,
    ) : OutboundMessage

    data class CommandFeedback(
        override val content: ChatComponent,
        override val overlay: Boolean = false,
    ) : OutboundMessage
}
