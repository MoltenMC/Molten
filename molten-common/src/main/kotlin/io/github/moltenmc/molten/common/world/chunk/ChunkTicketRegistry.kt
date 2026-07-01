package io.github.moltenmc.molten.common.world.chunk

class ChunkTicketRegistry {
    private val ticketsByChunk = LinkedHashMap<ChunkKey, MutableSet<ChunkTicket>>()

    fun addTicket(key: ChunkKey, ticket: ChunkTicket) {
        ticketsByChunk.getOrPut(key, ::linkedSetOf).add(ticket)
    }

    fun addTickets(key: ChunkKey, tickets: Iterable<ChunkTicket>) {
        tickets.forEach { ticket -> addTicket(key, ticket) }
    }

    fun ticketsFor(key: ChunkKey): Set<ChunkTicket> =
        ticketsByChunk[key]?.toSet().orEmpty()

    fun removeTicket(key: ChunkKey, ticket: ChunkTicket): Boolean {
        val tickets = ticketsByChunk[key] ?: return false
        val removed = tickets.remove(ticket)
        if (tickets.isEmpty()) {
            ticketsByChunk.remove(key)
        }
        return removed
    }

    fun cleanupExpired(currentTick: Long): Set<ChunkKey> {
        val unloadCandidates = linkedSetOf<ChunkKey>()
        val iterator = ticketsByChunk.iterator()
        while (iterator.hasNext()) {
            val (key, tickets) = iterator.next()
            tickets.removeIf { ticket -> ticket.isExpired(currentTick) }
            if (tickets.isEmpty()) {
                iterator.remove()
                unloadCandidates += key
            }
        }
        return unloadCandidates
    }
}
