package io.github.moltenmc.molten.common.spi

import io.github.moltenmc.molten.common.network.PacketCodec

interface PacketCodecProvider {
    fun codec(packetId: Int): PacketCodec<*>?
}
