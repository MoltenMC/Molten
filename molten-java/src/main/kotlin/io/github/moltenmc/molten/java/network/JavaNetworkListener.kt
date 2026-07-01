package io.github.moltenmc.molten.java.network

import java.net.InetSocketAddress

interface JavaNetworkListener {
    val localAddress: InetSocketAddress?

    fun bind(host: String, port: Int)

    fun close()
}
