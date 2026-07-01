package io.github.moltenmc.molten.common.world

import io.github.moltenmc.molten.common.registry.RegistryKey

data class BlockState(
    val key: RegistryKey,
    val properties: Map<String, String> = emptyMap(),
)
