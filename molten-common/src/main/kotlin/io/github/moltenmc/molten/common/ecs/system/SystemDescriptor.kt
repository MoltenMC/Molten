package io.github.moltenmc.molten.common.ecs.system

import io.github.moltenmc.molten.common.ecs.ComponentType

data class SystemDescriptor(
    val systemName: String,
    val phase: SystemPhase,
    val readComponents: Set<ComponentType> = emptySet(),
    val writeComponents: Set<ComponentType> = emptySet(),
    val optionalComponents: Set<ComponentType> = emptySet(),
    val requiredTags: Set<ComponentType> = emptySet(),
    val excludedTags: Set<ComponentType> = emptySet(),
    val threadingPolicy: ThreadingPolicy = ThreadingPolicy.REGION_LOCAL_PARALLEL,
    val priority: Int = 0,
) {
    fun conflictsWith(other: SystemDescriptor): Boolean =
        writeComponents.any { it in other.writeComponents || it in other.readComponents } ||
            readComponents.any { it in other.writeComponents }
}
