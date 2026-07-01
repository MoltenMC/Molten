package io.github.moltenmc.molten.java.network.packet

import io.github.moltenmc.molten.common.network.LazyPacketView

class PlayerMovementPacket(
    override val packetId: Int,
    val view: LazyPacketView,
) : JavaPacket
