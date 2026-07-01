package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.server.tick.TickRate

data class ServerConfiguration(
    val bindAddress: String,
    val javaPort: Int,
    val bedrockPort: Int,
    val tickRate: TickRate,
) {
    init {
        require(bindAddress.isNotBlank()) { "Bind address is required." }
        require(javaPort in PORT_RANGE) { "Java port is out of range." }
        require(bedrockPort in PORT_RANGE) { "Bedrock port is out of range." }
    }

    companion object {
        private val PORT_RANGE = 1..65535

        fun defaults(): ServerConfiguration =
            ServerConfiguration(
                bindAddress = "0.0.0.0",
                javaPort = 25565,
                bedrockPort = 19132,
                tickRate = TickRate.MinecraftDefault,
            )
    }
}
