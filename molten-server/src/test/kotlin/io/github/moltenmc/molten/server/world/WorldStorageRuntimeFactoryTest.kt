package io.github.moltenmc.molten.server.world

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.WorldStorageKind
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition
import io.github.moltenmc.molten.server.runtime.RuntimeMode
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorldStorageRuntimeFactoryTest {
    @Test
    fun createsJavaAnvilRuntimeForJavaBasedMode() {
        withTempDirectory { directory ->
            val paths = WorldStoragePaths(directory)
            WorldStorageRuntimeFactory(paths)
                .create(RuntimeDefinition.forMode(RuntimeMode.JAVA_BASED))
                .use { runtime ->
                    assertEquals(WorldStorageKind.JAVA_ANVIL, runtime.storageKind)
                    assertNotNull(runtime.chunks)
                    assertTrue(Files.isDirectory(paths.javaRegionDirectory))
                }
        }
    }

    @Test
    fun createsLevelDbRuntimeForBedrockBasedMode() {
        withTempDirectory { directory ->
            val paths = WorldStoragePaths(directory)
            WorldStorageRuntimeFactory(paths)
                .create(RuntimeDefinition.forMode(RuntimeMode.BEDROCK_BASED))
                .use { runtime ->
                    assertEquals(WorldStorageKind.BEDROCK_LEVELDB, runtime.storageKind)
                    assertNotNull(runtime.chunks)
                    assertTrue(Files.isDirectory(paths.bedrockDatabasePath))
                }
        }
    }

    @Test
    fun createdRuntimeCanLoadMissingChunkThroughWorldChunkService() {
        withTempDirectory { directory ->
            val runtime = WorldStorageRuntimeFactory(WorldStoragePaths(directory))
                .create(WorldStorageKind.JAVA_ANVIL)

            runtime.use {
                val result = it.storage.loadChunk(ChunkPos(0, 0)).get()

                assertEquals(null, result)
            }
        }
    }

    private fun withTempDirectory(block: (Path) -> Unit) {
        val directory = Files.createTempDirectory("molten-world-storage-test")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
