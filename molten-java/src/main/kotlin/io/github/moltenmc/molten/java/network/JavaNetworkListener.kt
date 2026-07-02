package io.github.moltenmc.molten.java.network

import java.net.InetSocketAddress

interface JavaNetworkListener {
    val localAddress: InetSocketAddress?

    fun bind(host: String, port: Int)

    fun tickIngressSessions(): Int = 0

    fun tickEgressSessions(): Int = tickSessions()

    fun tickSessions(): Int = 0

    fun close()
}
