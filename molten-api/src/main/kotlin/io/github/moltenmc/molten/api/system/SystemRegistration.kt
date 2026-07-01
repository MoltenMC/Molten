package io.github.moltenmc.molten.api.system

import io.github.moltenmc.molten.common.ecs.EntitySystem
import io.github.moltenmc.molten.common.ecs.system.SystemDescriptor

data class SystemRegistration(
    val descriptor: SystemDescriptor,
    val system: EntitySystem,
)
