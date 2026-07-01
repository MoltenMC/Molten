package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.DimensionId
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ChunkUnloadServiceTest {
    @Test
    fun unloadsCleanChunkWithoutSaving() {
        val key = key()
        val chunk = Chunk(key.position, emptyList())
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val storage = RecordingChunkStorage()
        val service = ChunkUnloadService(storage, store)

        val result = service.unload(key).get()

        assertIs<ChunkUnloadResult.Unloaded>(result)
        assertEquals(false, result.saved)
        assertEquals(0, storage.saveCalls)
        assertNull(store.get(key))
    }

    @Test
    fun savesDirtyChunkBeforeUnloading() {
        val key = key()
        val chunk = Chunk(
            position = key.position,
            sections = emptyList(),
            dirtyMarkers = setOf(DirtyMarker.NEEDS_SAVE),
        )
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val storage = RecordingChunkStorage()
        val service = ChunkUnloadService(storage, store)

        val result = service.unload(key).get()

        assertIs<ChunkUnloadResult.Unloaded>(result)
        assertEquals(true, result.saved)
        assertEquals(listOf(chunk), storage.savedChunks)
        assertNull(store.get(key))
    }

    @Test
    fun returnsMissingWhenChunkIsNotLoaded() {
        val key = key()
        val store = LoadedChunkStore()
        val service = ChunkUnloadService(RecordingChunkStorage(), store)

        val result = service.unload(key).get()

        assertIs<ChunkUnloadResult.Missing>(result)
        assertEquals(key, result.key)
    }

    @Test
    fun keepsDirtyChunkLoadedWhenSaveFails() {
        val key = key()
        val failure = IllegalStateException("save failed")
        val chunk = Chunk(
            position = key.position,
            sections = emptyList(),
            dirtyMarkers = setOf(DirtyMarker.NEEDS_SAVE),
        )
        val store = LoadedChunkStore().apply { put(key, chunk) }
        val storage = RecordingChunkStorage(saveResult = CompletableFuture.failedFuture(failure))
        val service = ChunkUnloadService(storage, store)

        val result = service.unload(key).get()

        assertIs<ChunkUnloadResult.Failed>(result)
        assertEquals(failure, result.cause)
        assertEquals(chunk, store.get(key))
    }

    private fun key(): ChunkKey =
        ChunkKey(
            worldId = WorldId(UUID(0, 1)),
            dimensionId = DimensionId(RegistryKey.parse("minecraft:overworld")),
            position = ChunkPos(1, 2),
        )

    private class RecordingChunkStorage(
        private val saveResult: CompletableFuture<Void> = CompletableFuture.completedFuture(null),
    ) : ChunkStorage {
        val savedChunks = mutableListOf<Chunk>()
        val saveCalls: Int
            get() = savedChunks.size

        override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
            CompletableFuture.completedFuture(null)

        override fun saveChunk(chunk: Chunk): CompletableFuture<Void> {
            savedChunks += chunk
            return saveResult
        }
    }
}
