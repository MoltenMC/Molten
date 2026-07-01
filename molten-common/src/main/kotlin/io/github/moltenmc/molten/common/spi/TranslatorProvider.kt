package io.github.moltenmc.molten.common.spi

import io.github.moltenmc.molten.common.registry.RegistryKey

interface TranslatorProvider {
    fun toInternal(key: RegistryKey): RegistryKey?

    fun toExternal(key: RegistryKey): RegistryKey?
}
