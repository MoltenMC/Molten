package io.github.moltenmc.molten.common.network

sealed interface PacketValidationResult {
    data object Accepted : PacketValidationResult

    data class Rejected(val reason: String, val disconnect: Boolean) : PacketValidationResult
}
