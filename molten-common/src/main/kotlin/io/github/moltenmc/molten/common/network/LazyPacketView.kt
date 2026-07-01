package io.github.moltenmc.molten.common.network

interface LazyPacketView : AutoCloseable {
    val readableBytes: Int

    fun copyDebugBytes(): ByteArray

    override fun close()
}
