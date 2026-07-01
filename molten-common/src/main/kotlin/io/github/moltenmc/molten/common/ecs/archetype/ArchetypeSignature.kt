package io.github.moltenmc.molten.common.ecs.archetype

import io.github.moltenmc.molten.common.ecs.ComponentType

data class ArchetypeSignature(
    val componentTypes: Set<ComponentType>,
) {
    fun with(componentType: ComponentType): ArchetypeSignature =
        ArchetypeSignature(componentTypes + componentType)

    fun without(componentType: ComponentType): ArchetypeSignature =
        ArchetypeSignature(componentTypes - componentType)
}
