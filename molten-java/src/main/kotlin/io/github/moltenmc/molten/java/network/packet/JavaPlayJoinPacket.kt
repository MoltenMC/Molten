package io.github.moltenmc.molten.java.network.packet

data class JavaPlayJoinPacket(
    override val packetId: Int,
    val entityId: Int,
    val hardcore: Boolean,
    val gameMode: Int,
    val previousGameMode: Int,
    val dimensionType: String,
    val worldName: String,
    val hashedSeed: Long,
) : JavaPacket
