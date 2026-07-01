package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.common.text.ChatComponent
import io.github.moltenmc.molten.java.network.codec.JavaSystemChatPacketCodec
import io.github.moltenmc.molten.java.network.packet.SystemChatPacket
import io.github.moltenmc.molten.java.status.JavaChatComponentJson

object JavaChatPackets {
    fun system(message: ChatComponent, overlay: Boolean = false): SystemChatPacket =
        SystemChatPacket(
            packetId = JavaSystemChatPacketCodec.PACKET_ID,
            contentJson = JavaChatComponentJson.encode(message),
            overlay = overlay,
        )
}
