package io.github.moltenmc.molten.common.world.chunk

enum class ChunkTicketType(val defaultPriority: Int) {
    PLAYER_VIEW(0),
    WORLD_SPAWN(10),
    ENTITY(20),
    REDSTONE(30),
    PLUGIN(40),
    TEMPORARY(100),
}
