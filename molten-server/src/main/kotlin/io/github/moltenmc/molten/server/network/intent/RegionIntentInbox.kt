package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class RegionIntentInbox : RegionIntentSink {
    private val queues = ConcurrentHashMap<RegionIntentKey, ConcurrentLinkedQueue<ServerIntent>>()

    override fun accept(worldId: WorldId, regionPos: RegionPos, intent: ServerIntent) {
        queues.computeIfAbsent(RegionIntentKey(worldId, regionPos)) { ConcurrentLinkedQueue() } += intent
    }

    fun size(key: RegionIntentKey): Int =
        queues[key]?.size ?: 0

    fun drain(key: RegionIntentKey): List<ServerIntent> {
        val queue = queues[key] ?: return emptyList()
        val drained = mutableListOf<ServerIntent>()
        while (true) {
            drained += queue.poll() ?: break
        }
        if (queue.isEmpty()) {
            queues.remove(key, queue)
        }
        return drained
    }
}
