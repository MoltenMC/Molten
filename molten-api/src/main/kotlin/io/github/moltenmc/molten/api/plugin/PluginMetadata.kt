package io.github.moltenmc.molten.api.plugin

data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val mainClass: String,
    val authors: List<String> = emptyList(),
    val description: String = "",
    val dependencies: Set<String> = emptySet(),
    val softDependencies: Set<String> = emptySet(),
    val runtimeCompatibility: Set<String> = emptySet(),
    val apiVersion: String,
) {
    init {
        require(id.isNotBlank()) { "Plugin id is required." }
        require(name.isNotBlank()) { "Plugin name is required." }
        require(version.isNotBlank()) { "Plugin version is required." }
        require(mainClass.isNotBlank()) { "Plugin main class is required." }
        require(apiVersion.isNotBlank()) { "Plugin API version is required." }
    }
}
