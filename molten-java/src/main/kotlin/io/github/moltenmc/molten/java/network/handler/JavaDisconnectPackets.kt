package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.codec.JavaConfigurationDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaLoginDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.codec.JavaPlayDisconnectPacketCodec
import io.github.moltenmc.molten.java.network.packet.ConfigurationDisconnectPacket
import io.github.moltenmc.molten.java.network.packet.LoginDisconnectPacket
import io.github.moltenmc.molten.java.network.packet.PlayDisconnectPacket
import io.github.moltenmc.molten.java.status.JavaJson

object JavaDisconnectPackets {
    fun login(reason: String): LoginDisconnectPacket =
        LoginDisconnectPacket(
            packetId = JavaLoginDisconnectPacketCodec.PACKET_ID,
            reasonJson = JavaJson.textComponent(reason),
        )

    fun configuration(reason: String): ConfigurationDisconnectPacket =
        ConfigurationDisconnectPacket(
            packetId = JavaConfigurationDisconnectPacketCodec.PACKET_ID,
            reasonJson = JavaJson.textComponent(reason),
        )

    fun play(reason: String): PlayDisconnectPacket =
        PlayDisconnectPacket(
            packetId = JavaPlayDisconnectPacketCodec.PACKET_ID,
            reasonJson = JavaJson.textComponent(reason),
        )
}
