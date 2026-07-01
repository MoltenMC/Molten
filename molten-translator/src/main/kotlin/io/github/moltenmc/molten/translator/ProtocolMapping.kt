package io.github.moltenmc.molten.translator

import io.github.moltenmc.molten.common.registry.RegistryKey

data class ProtocolMapping(
    val internalKey: RegistryKey,
    val externalKey: RegistryKey,
)
