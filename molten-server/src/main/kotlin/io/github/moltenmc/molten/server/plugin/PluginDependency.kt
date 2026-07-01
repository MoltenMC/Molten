package io.github.moltenmc.molten.server.plugin

data class PluginDependency(
    val pluginId: String,
    val required: Boolean,
)
