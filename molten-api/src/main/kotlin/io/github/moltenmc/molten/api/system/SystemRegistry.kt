package io.github.moltenmc.molten.api.system

import io.github.moltenmc.molten.common.ecs.EntitySystem
import io.github.moltenmc.molten.common.ecs.system.SystemDescriptor

interface SystemRegistry {
    fun register(descriptor: SystemDescriptor, system: EntitySystem)
}
