package io.github.moltenmc.molten.bedrock.network

enum class BedrockConnectionStep(val order: Int) {
    RAKNET_DISCOVERY(1),
    RAKNET_CONNECT(2),
    LOGIN(3),
    ENCRYPTION(4),
    RESOURCE_PACK(5),
    START_GAME(6),
    PLAY(7),
}
