package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketCodec
import io.github.moltenmc.molten.java.network.packet.JavaPacket

interface JavaPacketCodec<T : JavaPacket> : PacketCodec<T>
