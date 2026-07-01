package io.github.moltenmc.molten.common.ecs.system

enum class ThreadingPolicy {
    REGION_LOCAL_PARALLEL,
    REGION_LOCAL_ORDERED,
    GLOBAL_BARRIER,
}
