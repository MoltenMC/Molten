package io.github.moltenmc.molten.common.ecs.system

enum class SystemPhase {
    INPUT,
    PRE_PHYSICS,
    PHYSICS,
    AI,
    GAMEPLAY,
    POST_GAMEPLAY,
    REPLICATION,
    CLEANUP,
}
