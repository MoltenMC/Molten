package io.github.moltenmc.molten.common.performance

enum class PerformanceMetric {
    TPS,
    MSPT,
    PACKET_DECODE_LATENCY,
    PACKET_ENCODE_LATENCY,
    CHUNK_LOAD_LATENCY,
    CHUNK_SAVE_LATENCY,
    ENTITY_UPDATE_COST,
    ECS_QUERY_COST,
    GC_PAUSE_TIME,
    MEMORY_USAGE,
    REGION_QUEUE_DEPTH,
    NETWORK_OUTBOUND_QUEUE_SIZE,
}
