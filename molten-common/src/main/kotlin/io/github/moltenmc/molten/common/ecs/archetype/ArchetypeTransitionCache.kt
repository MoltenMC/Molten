package io.github.moltenmc.molten.common.ecs.archetype

import io.github.moltenmc.molten.common.ecs.ComponentType

class ArchetypeTransitionCache {
    private val addedTransitions = HashMap<Pair<ArchetypeId, ComponentType>, ArchetypeId>()
    private val removedTransitions = HashMap<Pair<ArchetypeId, ComponentType>, ArchetypeId>()

    fun cacheAdded(from: ArchetypeId, componentType: ComponentType, to: ArchetypeId) {
        addedTransitions[from to componentType] = to
    }

    fun cacheRemoved(from: ArchetypeId, componentType: ComponentType, to: ArchetypeId) {
        removedTransitions[from to componentType] = to
    }

    fun added(from: ArchetypeId, componentType: ComponentType): ArchetypeId? =
        addedTransitions[from to componentType]

    fun removed(from: ArchetypeId, componentType: ComponentType): ArchetypeId? =
        removedTransitions[from to componentType]
}
