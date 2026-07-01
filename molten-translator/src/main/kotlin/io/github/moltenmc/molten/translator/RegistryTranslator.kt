package io.github.moltenmc.molten.translator

import io.github.moltenmc.molten.common.registry.RegistryKey

interface RegistryTranslator {
    fun toExternal(internalKey: RegistryKey): RegistryKey?

    fun toInternal(externalKey: RegistryKey): RegistryKey?
}
