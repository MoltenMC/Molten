package io.github.moltenmc.molten.common.network.security

data class NetworkSecurityPolicy(
    val maxPacketBytes: Int,
    val maxBatchBytes: Int,
    val maxPluginMessageBytes: Int,
    val protections: Set<NetworkProtection>,
) {
    init {
        require(maxPacketBytes > 0) { "Maximum packet size must be positive." }
        require(maxBatchBytes > 0) { "Maximum batch size must be positive." }
        require(maxPluginMessageBytes > 0) { "Maximum plugin message size must be positive." }
    }

    companion object {
        val defaults: NetworkSecurityPolicy = NetworkSecurityPolicy(
            maxPacketBytes = 2 * 1024 * 1024,
            maxBatchBytes = 8 * 1024 * 1024,
            maxPluginMessageBytes = 32 * 1024,
            protections = NetworkProtection.entries.toSet(),
        )
    }
}
