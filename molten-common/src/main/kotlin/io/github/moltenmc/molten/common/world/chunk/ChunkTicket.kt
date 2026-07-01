package io.github.moltenmc.molten.common.world.chunk

data class ChunkTicket(
    val type: ChunkTicketType,
    val owner: String,
    val radius: Int = 0,
    val priority: Int = type.defaultPriority,
    val expiresAtTick: Long? = null,
) {
    init {
        require(owner.isNotBlank()) { "Chunk ticket owner must not be blank." }
        require(radius >= 0) { "Chunk ticket radius must not be negative." }
        require(priority >= 0) { "Chunk ticket priority must not be negative." }
        require(expiresAtTick == null || expiresAtTick >= 0) {
            "Chunk ticket expiration tick must not be negative."
        }
    }

    fun isExpired(currentTick: Long): Boolean =
        expiresAtTick?.let { currentTick >= it } ?: false

    companion object {
        val priorityOrder: Comparator<ChunkTicket> =
            compareBy<ChunkTicket> { it.priority }
                .thenBy { it.type.ordinal }
                .thenBy { it.owner }
    }
}
