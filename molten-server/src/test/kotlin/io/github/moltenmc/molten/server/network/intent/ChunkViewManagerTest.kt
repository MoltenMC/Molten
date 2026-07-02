package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.world.ChunkPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChunkViewManagerTest {
    @Test
    fun loadsAllChunksOnFirstMove() {
        val manager = ChunkViewManager(chunkViewDistance = 2)
        val playerId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        
        val update = manager.updateChunkView(playerId, Vec3d(0.0, 64.0, 0.0))
        
        // Should load all chunks in 2x2 view distance (5x5 = 25 chunks)
        assertEquals(25, update.chunksToLoad.size)
        assertTrue(update.chunksToUnload.isEmpty())
    }

    @Test
    fun loadsNewChunksWhenMoving() {
        val manager = ChunkViewManager(chunkViewDistance = 1)
        val playerId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        
        // Initial position at chunk (0, 0)
        manager.updateChunkView(playerId, Vec3d(0.0, 64.0, 0.0))
        
        // Move to chunk (2, 0) - 2 chunks away
        val update = manager.updateChunkView(playerId, Vec3d(32.0, 64.0, 0.0))
        
        // Should load new chunks and unload old ones
        assertTrue(update.chunksToLoad.isNotEmpty())
        assertTrue(update.chunksToUnload.isNotEmpty())
    }

    @Test
    fun noChangeWhenMovingWithinSameChunk() {
        val manager = ChunkViewManager(chunkViewDistance = 1)
        val playerId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        
        manager.updateChunkView(playerId, Vec3d(0.0, 64.0, 0.0))
        
        // Move within same chunk
        val update = manager.updateChunkView(playerId, Vec3d(8.0, 64.0, 8.0))
        
        assertTrue(update.chunksToLoad.isEmpty())
        assertTrue(update.chunksToUnload.isEmpty())
    }

    @Test
    fun unloadsAllChunksWhenPlayerRemoved() {
        val manager = ChunkViewManager(chunkViewDistance = 1)
        val playerId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        
        manager.updateChunkView(playerId, Vec3d(0.0, 64.0, 0.0))
        
        val unloaded = manager.removePlayer(playerId)
        
        // Should unload all chunks in view (3x3 = 9 chunks)
        assertEquals(9, unloaded.size)
    }

    @Test
    fun publishesChunkLoadEvents() {
        val publishedEvents = mutableListOf<ChunkLoadEvent>()
        val publisher = object : ChunkLoadPublisher {
            override fun publishChunkLoadEvents(event: ChunkLoadEvent) {
                publishedEvents.add(event)
            }
        }
        
        val manager = ChunkViewManager(chunkViewDistance = 1, publisher)
        val playerId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        
        manager.updateChunkView(playerId, Vec3d(0.0, 64.0, 0.0))
        
        assertEquals(1, publishedEvents.size)
        assertEquals(playerId, publishedEvents[0].playerId)
        assertTrue(publishedEvents[0].chunksToLoad.isNotEmpty())
    }

    @Test
    fun handlesMultiplePlayers() {
        val manager = ChunkViewManager(chunkViewDistance = 1)
        val player1 = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        val player2 = EntityId.of(2, generation = 0, EntityKind.PLAYER)
        
        manager.updateChunkView(player1, Vec3d(0.0, 64.0, 0.0))
        manager.updateChunkView(player2, Vec3d(32.0, 64.0, 0.0))
        
        // Both players should have their own chunk views
        val update1 = manager.updateChunkView(player1, Vec3d(8.0, 64.0, 8.0))
        val update2 = manager.updateChunkView(player2, Vec3d(40.0, 64.0, 8.0))
        
        // Player 1 should have no change (within same chunk)
        assertTrue(update1.chunksToLoad.isEmpty())
        // Player 2 should have no change (within same chunk)
        assertTrue(update2.chunksToLoad.isEmpty())
    }
}
