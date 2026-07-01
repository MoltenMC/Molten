package io.github.moltenmc.molten.bedrock

import io.github.moltenmc.molten.bedrock.world.leveldb.BedrockChunkRecordKey
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeDataSource
import io.github.moltenmc.molten.translator.block.BedrockRuntimeBlockMapping
import kotlin.test.Test
import kotlin.test.assertEquals

class BedrockProtocolModuleTest {
    @Test
    fun createsLevelDbMapperUsingBundledRuntimeMappingsByDefault() {
        val module = BedrockProtocolModule()
        module.start()

        val mapper = module.createLevelDbChunkMapper()
        val position = ChunkPos(0, 0)
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 0).encode()
        val payload = byteArrayOf(8, 1, 1, 1, 1)

        val chunk = mapper.toChunk(position, mapOf(subChunkKey to payload))

        assertEquals(
            BlockState(RegistryKey.parse("minecraft:stone")),
            chunk.sections.single().blocks.palette.single(),
        )
    }

    @Test
    fun createsLevelDbMapperUsingBundledBiomeMappingsByDefault() {
        val module = BedrockProtocolModule()
        module.start()

        val mapper = module.createLevelDbChunkMapper()
        val position = ChunkPos(0, 0)
        val data2dKey = BedrockChunkRecordKey.data2d(position).encode()
        val data2dPayload = ByteArray(512 + 256) { index ->
            if (index == 512) 4 else 1
        }

        val chunk = mapper.toChunk(position, mapOf(data2dKey to data2dPayload))

        assertEquals(
            RegistryKey.parse("minecraft:forest"),
            chunk.sections.single().biomes.valueAt(0),
        )
    }

    @Test
    fun createsLevelDbMapperUsingBootstrappedRuntimeMappings() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val module = BedrockProtocolModule(
            blockRuntimeDataSources = listOf(
                BedrockBlockRuntimeDataSource.static(
                    listOf(BedrockRuntimeBlockMapping(7, stone)),
                ),
            ),
        )
        module.start()

        val mapper = module.createLevelDbChunkMapper()
        val position = ChunkPos(0, 0)
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 0).encode()
        val payload = byteArrayOf(8, 1, 1, 1, 7)

        val chunk = mapper.toChunk(position, mapOf(subChunkKey to payload))

        assertEquals(stone, chunk.sections.single().blocks.palette.single())
    }
}
