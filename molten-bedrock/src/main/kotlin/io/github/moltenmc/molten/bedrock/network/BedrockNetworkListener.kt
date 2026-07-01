package io.github.moltenmc.molten.bedrock.network

interface BedrockNetworkListener {
    fun bind(host: String, port: Int)

    fun close()
}
