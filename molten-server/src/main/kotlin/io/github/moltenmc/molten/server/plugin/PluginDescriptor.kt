package io.github.moltenmc.molten.server.plugin

import io.github.moltenmc.molten.api.plugin.PluginLifecyclePhase
import io.github.moltenmc.molten.api.plugin.PluginMetadata

data class PluginDescriptor(
    val metadata: PluginMetadata,
    val metadataFile: String = "molten-plugin.yml",
    val lifecyclePhase: PluginLifecyclePhase = PluginLifecyclePhase.LOAD,
)
