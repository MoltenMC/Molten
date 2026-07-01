package io.github.moltenmc.molten.common.ecs

enum class EntityLifecycleState {
    CREATED,
    ACTIVE,
    DESPAWN_REQUESTED,
    DESTROYED,
}
