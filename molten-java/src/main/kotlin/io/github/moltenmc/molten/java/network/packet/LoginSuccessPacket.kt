package io.github.moltenmc.molten.java.network.packet

import java.util.UUID

data class LoginSuccessPacket(
    override val packetId: Int,
    val uuid: UUID,
    val username: String,
    val properties: List<LoginSuccessProperty> = emptyList(),
) : JavaPacket
