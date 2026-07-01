package io.github.moltenmc.molten.common.world.chunk

class LoadedChunkStore {
    private val chunks = LinkedHashMap<ChunkKey, Chunk>()

    fun put(key: ChunkKey, chunk: Chunk): Chunk? =
        chunks.put(key, chunk)

    fun get(key: ChunkKey): Chunk? =
        chunks[key]

    fun contains(key: ChunkKey): Boolean =
        key in chunks

    fun remove(key: ChunkKey): Chunk? =
        chunks.remove(key)

    fun keys(): Set<ChunkKey> =
        chunks.keys.toSet()
}
