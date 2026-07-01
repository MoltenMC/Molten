package io.github.moltenmc.molten.common.network

data class ProtocolContext(
    val protocolVersion: Int,
    val direction: PacketDirection,
)
