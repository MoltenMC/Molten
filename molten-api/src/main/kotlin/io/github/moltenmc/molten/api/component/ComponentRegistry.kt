package io.github.moltenmc.molten.api.component

import io.github.moltenmc.molten.common.ecs.Component
import io.github.moltenmc.molten.common.ecs.ComponentType
import kotlin.reflect.KClass

interface ComponentRegistry {
    fun <T : Component> register(componentClass: KClass<T>): ComponentType
}
