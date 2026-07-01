package io.github.moltenmc.molten.translator.resource

import io.github.moltenmc.molten.common.registry.RegistryKey

class ResourceKeyNormalizer(
    private val aliases: Map<RegistryKey, RegistryKey> = emptyMap(),
) {
    fun normalize(key: RegistryKey): RegistryKey = aliases[key] ?: key
}
