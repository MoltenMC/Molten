package io.github.moltenmc.molten.java.network

interface JavaNetworkListener {
    fun bind(host: String, port: Int)

    fun close()
}
