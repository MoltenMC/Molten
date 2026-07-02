package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.world.ChunkPos

/**
 * Manages chunk view for players, triggering load/unload events as they move.
 */
class ChunkViewManager(
    private val chunkViewDistance: Int = 8, // Default view distance in chunks
    private val chunkLoadPublisher: ChunkLoadPublisher? = null,
) {
    private val playerChunkViews = mutableMapOf<EntityId, ChunkViewState>()

    /**
     * Updates chunk view for a player based on their new position.
     * Returns chunks that should be loaded and chunks that should be unloaded.
     */
    fun updateChunkView(
        playerId: EntityId,
        newPosition: Vec3d,
    ): ChunkViewUpdate {
        val newChunkPos = ChunkPos(
            x = kotlin.math.floor(newPosition.x / 16.0).toInt(),
            z = kotlin.math.floor(newPosition.z / 16.0).toInt(),
        )
        val previousState = playerChunkViews[playerId]
        
        val previousChunkPos = previousState?.chunkPos
        val chunksToLoad = mutableSetOf<ChunkPos>()
        val chunksToUnload = mutableSetOf<ChunkPos>()
        
        if (previousChunkPos == null) {
            // First time seeing this player - load all chunks in view
            val chunksInView = getChunksInView(newChunkPos)
            chunksToLoad.addAll(chunksInView)
        } else {
            // Calculate chunks that entered view
            val newChunksInView = getChunksInView(newChunkPos)
            val previousChunksInView = getChunksInView(previousChunkPos)
            
            chunksToLoad.addAll(newChunksInView - previousChunksInView)
            chunksToUnload.addAll(previousChunksInView - newChunksInView)
        }
        
        // Update player state
        playerChunkViews[playerId] = ChunkViewState(newChunkPos)
        
        // Publish load/unload events if publisher is available
        chunkLoadPublisher?.publishChunkLoadEvents(
            ChunkLoadEvent(
                playerId = playerId,
                chunksToLoad = chunksToLoad.toList(),
                chunksToUnload = chunksToUnload.toList(),
            ),
        )
        
        return ChunkViewUpdate(chunksToLoad.toList(), chunksToUnload.toList())
    }

    /**
     * Removes a player from chunk view management.
     * Returns all chunks that were in their view.
     */
    fun removePlayer(playerId: EntityId): List<ChunkPos> {
        val state = playerChunkViews.remove(playerId)
        return if (state != null) {
            val chunksInView = getChunksInView(state.chunkPos)
            chunkLoadPublisher?.publishChunkLoadEvents(
                ChunkLoadEvent(
                    playerId = playerId,
                    chunksToLoad = emptyList(),
                    chunksToUnload = chunksInView.toList(),
                ),
            )
            chunksInView.toList()
        } else {
            emptyList()
        }
    }

    /**
     * Gets all chunk positions within view distance of a center chunk.
     */
    private fun getChunksInView(centerChunk: ChunkPos): Set<ChunkPos> {
        val chunks = mutableSetOf<ChunkPos>()
        for (x in -chunkViewDistance..chunkViewDistance) {
            for (z in -chunkViewDistance..chunkViewDistance) {
                chunks.add(ChunkPos(centerChunk.x + x, centerChunk.z + z))
            }
        }
        return chunks
    }

    /**
     * State tracking a player's chunk view.
     */
    private data class ChunkViewState(
        val chunkPos: ChunkPos,
    )
}

/**
 * Result of a chunk view update.
 */
data class ChunkViewUpdate(
    val chunksToLoad: List<ChunkPos>,
    val chunksToUnload: List<ChunkPos>,
)

/**
 * Event representing chunk load/unload operations for a player.
 */
data class ChunkLoadEvent(
    val playerId: EntityId,
    val chunksToLoad: List<ChunkPos>,
    val chunksToUnload: List<ChunkPos>,
)

/**
 * Interface for publishing chunk load events.
 */
interface ChunkLoadPublisher {
    fun publishChunkLoadEvents(event: ChunkLoadEvent)
}

/**
 * No-op implementation of ChunkLoadPublisher for testing.
 */
class NoopChunkLoadPublisher : ChunkLoadPublisher {
    override fun publishChunkLoadEvents(event: ChunkLoadEvent) {
        // No-op
    }
}
