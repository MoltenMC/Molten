package io.github.moltenmc.molten.translator.capability

data class CapabilityResolution(
    val flag: CapabilityFlag,
    val behavior: UnsupportedFeatureBehavior,
    val reason: String,
)

enum class UnsupportedFeatureBehavior {
    EMULATE,
    FALLBACK,
    HIDE,
    REJECT,
    DISCONNECT,
}
