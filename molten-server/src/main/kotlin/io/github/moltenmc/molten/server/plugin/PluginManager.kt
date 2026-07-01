package io.github.moltenmc.molten.server.plugin

import io.github.moltenmc.molten.api.plugin.MoltenPlugin

interface PluginManager {
    fun load(plugin: MoltenPlugin)

    fun enableAll()

    fun disableAll()
}
