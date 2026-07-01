package io.github.moltenmc.molten.common.ecs

import kotlin.reflect.KClass

data class ComponentType(
    val type: KClass<out Component>,
    val storagePolicy: ComponentStoragePolicy,
)
