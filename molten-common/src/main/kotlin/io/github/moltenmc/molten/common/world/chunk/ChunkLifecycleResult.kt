package io.github.moltenmc.molten.common.world.chunk

data class ChunkLifecycleResult(
    val unloadCandidates: Set<ChunkKey>,
    val unloadResults: List<ChunkUnloadResult>,
)
