package io.github.moltenmc.molten.common.runtime

interface RuntimeModule {
    val name: String

    fun start()

    fun stop()
}
