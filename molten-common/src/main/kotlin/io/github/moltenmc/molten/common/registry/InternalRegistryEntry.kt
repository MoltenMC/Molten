package io.github.moltenmc.molten.common.registry

data class InternalRegistryEntry(
    val registryKey: RegistryKey,
    val internalId: Int,
    val properties: Map<String, String> = emptyMap(),
    val defaultState: RegistryKey? = null,
    val capabilities: Set<String> = emptySet(),
    val javaMapping: RegistryKey? = null,
    val bedrockMapping: RegistryKey? = null,
)
