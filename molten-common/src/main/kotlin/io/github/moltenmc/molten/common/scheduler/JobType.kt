package io.github.moltenmc.molten.common.scheduler

enum class JobType {
    NETWORK_DECODE,
    NETWORK_ENCODE,
    REGION_TICK,
    SYSTEM_EXECUTION,
    CHUNK_LOAD,
    CHUNK_SAVE,
    PLUGIN_TASK,
    ASYNC_EVENT,
    PATHFINDING,
    COMPRESSION,
}
