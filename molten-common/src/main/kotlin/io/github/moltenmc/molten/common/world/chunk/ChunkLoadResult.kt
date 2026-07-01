package io.github.moltenmc.molten.common.world.chunk

sealed interface ChunkLoadResult {
    val request: ChunkLoadRequest

    data class Loaded(
        override val request: ChunkLoadRequest,
        val chunk: Chunk,
    ) : ChunkLoadResult

    data class Missing(
        override val request: ChunkLoadRequest,
    ) : ChunkLoadResult

    data class Failed(
        override val request: ChunkLoadRequest,
        val cause: Throwable,
    ) : ChunkLoadResult
}
