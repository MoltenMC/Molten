package io.github.moltenmc.molten.common.network

interface PacketCodec<T> {
    fun decode(buffer: ByteArray, protocolContext: ProtocolContext): T

    fun encode(packet: T, protocolContext: ProtocolContext): ByteArray

    fun validate(packet: T, sessionContext: SessionContext): PacketValidationResult
}
