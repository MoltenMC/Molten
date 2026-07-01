package io.github.moltenmc.molten.common.ecs

fun interface EntitySystem {
    fun tick(context: SystemContext)
}
