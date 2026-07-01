package io.github.moltenmc.molten.server

data class ServerConfiguration(
    val bindAddress: String,
    val javaPort: Int,
    val bedrockPort: Int,
) {
    init {
        require(bindAddress.isNotBlank()) { "Bind address is required." }
        require(javaPort in PORT_RANGE) { "Java port is out of range." }
        require(bedrockPort in PORT_RANGE) { "Bedrock port is out of range." }
    }

    companion object {
        private val PORT_RANGE = 1..65535

        fun defaults(): ServerConfiguration = ServerConfiguration("0.0.0.0", 25565, 19132)
    }
}
