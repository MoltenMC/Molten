package io.github.moltenmc.molten.server.bootstrap

import io.github.moltenmc.molten.common.runtime.RuntimeModule
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition

data class BootstrapPlan(
    val runtimeDefinition: RuntimeDefinition,
    val modules: List<RuntimeModule>,
)
