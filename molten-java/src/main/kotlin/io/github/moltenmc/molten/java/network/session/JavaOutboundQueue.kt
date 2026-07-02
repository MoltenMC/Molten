package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.java.network.message.JavaOutboundMessageAdapter
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import java.util.ArrayDeque

class JavaOutboundQueue(
    private val adapter: JavaOutboundMessageAdapter = JavaOutboundMessageAdapter(),
) {
    private val messages = ArrayDeque<OutboundMessage>()

    val size: Int
        get() = messages.size

    fun enqueue(message: OutboundMessage) {
        messages.addLast(message)
    }

    fun drainMessages(): List<OutboundMessage> {
        val drained = ArrayList<OutboundMessage>(messages.size)
        while (messages.isNotEmpty()) {
            drained += messages.removeFirst()
        }
        return drained
    }

    fun drainPackets(state: JavaProtocolState): List<JavaPacket> {
        if (state != JavaProtocolState.PLAY) {
            return emptyList()
        }
        return drainMessages().flatMap(adapter::toPackets)
    }
}
