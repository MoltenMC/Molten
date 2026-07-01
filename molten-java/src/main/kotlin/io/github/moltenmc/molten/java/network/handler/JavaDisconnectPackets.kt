package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.codec.JavaLoginDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import io.github.moltenmc.molten.java.status.JavaJson

object JavaDisconnectPackets {
    fun login(reason: String): LoginDisconnectPacket =
        LoginDisconnectPacket(
            packetId = JavaLoginDisconnectPacketCodec.PACKET_ID,
            reasonJson = JavaJson.textComponent(reason),
        )
}
