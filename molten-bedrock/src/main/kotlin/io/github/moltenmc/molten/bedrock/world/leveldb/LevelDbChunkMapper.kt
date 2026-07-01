package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.chunk.Chunk
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer

interface LevelDbChunkMapper {
    fun toChunk(position: ChunkPos, records: Map<ByteArray, ByteArray>): Chunk

    fun toRecords(chunk: Chunk): Map<ByteArray, ByteArray>
}

class RawPreservingLevelDbChunkMapper(
    private val subChunkDecoder: BedrockSubChunkDecoder = BedrockSubChunkDecoder(),
) : LevelDbChunkMapper {
    private val defaultBlockPalette = PalettedContainer(
        palette = listOf(BlockState(RegistryKey.parse("minecraft:air"))),
        packedData = longArrayOf(0),
    )
    private val defaultBiomePalette = PalettedContainer(
        palette = listOf(RegistryKey.parse("minecraft:plains")),
        packedData = longArrayOf(0),
    )

    override fun toChunk(position: ChunkPos, records: Map<ByteArray, ByteArray>): Chunk =
        Chunk(
            position = position,
            sections = records.entries
                .mapNotNull { (key, value) ->
                    BedrockChunkRecordKey.subChunkY(key)?.let { sectionY ->
                        subChunkDecoder.decode(sectionY, value) ?: emptySection(sectionY)
                    }
                }
                .distinctBy(ChunkSection::y)
                .sortedBy(ChunkSection::y),
            rawData = mapOf(BedrockChunkRawPayload.RAW_DATA_KEY to BedrockChunkRawPayload.encode(records)),
        )

    override fun toRecords(chunk: Chunk): Map<ByteArray, ByteArray> =
        BedrockChunkRawPayload.decode(chunk.rawData[BedrockChunkRawPayload.RAW_DATA_KEY])

    private fun emptySection(y: Int): ChunkSection =
        ChunkSection(
            y = y,
            blocks = defaultBlockPalette,
            biomes = defaultBiomePalette,
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )
}
