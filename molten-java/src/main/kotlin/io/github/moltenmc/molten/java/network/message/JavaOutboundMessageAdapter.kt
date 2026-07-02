package io.github.moltenmc.molten.java.network.message

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.java.network.handler.JavaChatPackets
import io.github.moltenmc.molten.java.network.packet.JavaPacket

class JavaOutboundMessageAdapter {
    fun toPackets(message: OutboundMessage): List<JavaPacket> =
        when (message) {
            is OutboundMessage.System -> listOf(JavaChatPackets.system(message.content, message.overlay))
            is OutboundMessage.CommandFeedback -> listOf(JavaChatPackets.system(message.content, message.overlay))
        }
}
