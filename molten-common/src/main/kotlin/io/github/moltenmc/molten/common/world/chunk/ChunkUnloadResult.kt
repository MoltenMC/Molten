package io.github.moltenmc.molten.common.world.chunk

sealed interface ChunkUnloadResult {
    val key: ChunkKey

    data class Unloaded(
        override val key: ChunkKey,
        val saved: Boolean,
    ) : ChunkUnloadResult

    data class Missing(
        override val key: ChunkKey,
    ) : ChunkUnloadResult

    data class Failed(
        override val key: ChunkKey,
        val chunk: Chunk,
        val cause: Throwable,
    ) : ChunkUnloadResult
}
