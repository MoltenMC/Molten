package io.github.moltenmc.molten.common.spi

import io.github.moltenmc.molten.common.registry.InternalRegistryEntry
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.registry.RegistryType

interface RegistryProvider {
    fun find(type: RegistryType, key: RegistryKey): InternalRegistryEntry?
}
