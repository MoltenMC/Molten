package io.github.moltenmc.molten.java.network.packet

import java.util.UUID

data class LoginStartPacket(
    override val packetId: Int,
    val name: String,
    val playerUuid: UUID,
) : JavaPacket
