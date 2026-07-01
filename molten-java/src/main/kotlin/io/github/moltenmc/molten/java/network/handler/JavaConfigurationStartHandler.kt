package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.codec.JavaFinishConfigurationPacketCodec
import io.github.moltenmc.molten.java.network.packet.FinishConfigurationPacket
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState

class JavaConfigurationStartHandler {
    fun configurationPacketsFor(sessionHolder: JavaSessionHolder): List<JavaPacket> {
        require(sessionHolder.state == JavaProtocolState.CONFIGURATION) {
            "Configuration packets can only be created in CONFIGURATION state."
        }
        return listOf(
            FinishConfigurationPacket(packetId = JavaFinishConfigurationPacketCodec.PACKET_ID),
        )
    }
}
