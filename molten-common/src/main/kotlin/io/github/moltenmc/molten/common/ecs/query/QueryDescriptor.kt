package io.github.moltenmc.molten.common.ecs.query

import io.github.moltenmc.molten.common.ecs.ComponentType

data class QueryDescriptor(
    val required: Set<ComponentType>,
    val optional: Set<ComponentType> = emptySet(),
    val excluded: Set<ComponentType> = emptySet(),
)
