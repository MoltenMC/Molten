package io.github.moltenmc.molten.translator.registry

import io.github.moltenmc.molten.common.registry.FallbackPolicy
import io.github.moltenmc.molten.common.registry.RegistryType

data class RegistryMappingPolicy(
    val registryType: RegistryType,
    val fallbackPolicy: FallbackPolicy,
    val stableDuringRuntime: Boolean = true,
)
