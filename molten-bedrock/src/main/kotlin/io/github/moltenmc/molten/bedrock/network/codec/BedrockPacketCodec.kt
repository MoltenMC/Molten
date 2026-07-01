package io.github.moltenmc.molten.bedrock.network.codec

import io.github.moltenmc.molten.bedrock.network.packet.BedrockPacket
import io.github.moltenmc.molten.common.network.PacketCodec

interface BedrockPacketCodec<T : BedrockPacket> : PacketCodec<T>
