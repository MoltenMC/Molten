package io.github.moltenmc.molten.server.tick

enum class TickPipelineStep(val order: Int) {
    NETWORK_INGRESS(1),
    INTENT_ROUTING(2),
    REGION_SIMULATION(3),
    COMMAND_BUFFER_FLUSH(4),
    SYNC_EVENTS(5),
    WORLD_UPDATE(6),
    REPLICATION_BUILD(7),
    PROTOCOL_ENCODE(8),
    NETWORK_EGRESS(9),
    CLEANUP(10),
}
