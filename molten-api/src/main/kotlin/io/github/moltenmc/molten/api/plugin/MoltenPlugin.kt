package io.github.moltenmc.molten.api.plugin

interface Plugin {
    fun onLoad(context: PluginContext)

    fun onEnable()

    fun onDisable()
}

typealias MoltenPlugin = Plugin
