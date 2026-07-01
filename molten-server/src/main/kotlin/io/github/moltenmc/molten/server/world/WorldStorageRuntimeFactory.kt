package io.github.moltenmc.molten.server.world

import io.github.moltenmc.molten.bedrock.world.leveldb.DefaultLevelDbStorageAdapter
import io.github.moltenmc.molten.common.world.WorldStorageKind
import io.github.moltenmc.molten.common.world.chunk.ChunkStorage
import io.github.moltenmc.molten.common.world.chunk.WorldChunkService
import io.github.moltenmc.molten.java.world.anvil.DefaultAnvilStorageAdapter
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition
import java.nio.file.Files

class WorldStorageRuntimeFactory(
    private val paths: WorldStoragePaths,
) {
    fun create(runtimeDefinition: RuntimeDefinition): WorldStorageRuntime =
        create(runtimeDefinition.primaryStorage)

    fun create(storageKind: WorldStorageKind): WorldStorageRuntime {
        val storage = createStorage(storageKind)
        return WorldStorageRuntime(
            storageKind = storageKind,
            storage = storage,
            chunks = WorldChunkService.create(storage),
            closeStorage = { storage.close() },
        )
    }

    private fun createStorage(storageKind: WorldStorageKind): CloseableChunkStorage =
        when (storageKind) {
            WorldStorageKind.JAVA_ANVIL -> {
                Files.createDirectories(paths.javaRegionDirectory)
                CloseableChunkStorage(DefaultAnvilStorageAdapter(paths.javaRegionDirectory))
            }
            WorldStorageKind.BEDROCK_LEVELDB -> {
                Files.createDirectories(paths.bedrockDatabasePath)
                CloseableChunkStorage(DefaultLevelDbStorageAdapter(paths.bedrockDatabasePath))
            }
        }

    private class CloseableChunkStorage(
        private val delegate: ChunkStorage,
    ) : ChunkStorage by delegate, AutoCloseable {
        override fun close() {
            (delegate as AutoCloseable).close()
        }
    }
}
