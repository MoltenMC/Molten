package io.github.moltenmc.molten.bedrock.network

import java.net.InetSocketAddress

interface BedrockNetworkListener {
    val localAddress: InetSocketAddress?

    fun bind(host: String, port: Int)

    fun close()
}
