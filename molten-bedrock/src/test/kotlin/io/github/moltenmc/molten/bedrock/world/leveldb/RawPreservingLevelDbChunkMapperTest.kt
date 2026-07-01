package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.translator.block.MapBackedBedrockBlockRuntimeIdTranslator
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RawPreservingLevelDbChunkMapperTest {
    private val mapper = RawPreservingLevelDbChunkMapper()

    @Test
    fun preservesRawRecordsInChunkRawData() {
        val key = byteArrayOf(1, 2, 3)
        val value = byteArrayOf(4, 5, 6)

        val chunk = mapper.toChunk(ChunkPos(1, 2), mapOf(key to value))
        val records = mapper.toRecords(chunk)

        assertEquals(ChunkPos(1, 2), chunk.position)
        assertEquals(1, records.size)
        val record = records.entries.single()
        assertContentEquals(key, record.key)
        assertContentEquals(value, record.value)
    }

    @Test
    fun createsSectionSkeletonsForSubChunkRecords() {
        val position = ChunkPos(1, 2)
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = -1).encode()

        val chunk = mapper.toChunk(position, mapOf(subChunkKey to byteArrayOf(8, 0, 1)))

        val section = chunk.sections.single()
        assertEquals(-1, section.y)
        assertEquals(RegistryKey.parse("minecraft:air"), section.blocks.palette.single().key)
        assertEquals(RegistryKey.parse("minecraft:plains"), section.biomes.palette.single())
    }

    @Test
    fun decodesRuntimePaletteFromSubChunkRecords() {
        val mappedState = BlockState(RegistryKey.parse("minecraft:stone"))
        val mapper = RawPreservingLevelDbChunkMapper(
            subChunkDecoder = BedrockSubChunkDecoder(
                runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(7 to mappedState)),
            ),
        )
        val position = ChunkPos(1, 2)
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 3).encode()
        val payload = byteArrayOf(
            8,
            1,
            1,
            1,
            7,
        )

        val chunk = mapper.toChunk(position, mapOf(subChunkKey to payload))

        val section = chunk.sections.single()
        assertEquals(3, section.y)
        assertEquals(mappedState, section.blocks.palette.single())
        assertEquals(0, section.blocks.packedData.size)
    }
}
