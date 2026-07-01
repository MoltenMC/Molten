package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.nbt.NbtValue
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
    private val data2DDecoder: BedrockData2DDecoder = BedrockData2DDecoder(),
    private val dimension: Int = BedrockChunkRecordKey.OVERWORLD_DIMENSION,
) : LevelDbChunkMapper {
    private val defaultBlockPalette = PalettedContainer(
        palette = listOf(BlockState(RegistryKey.parse("minecraft:air"))),
        packedData = longArrayOf(0),
    )
    private val defaultBiomePalette = PalettedContainer(
        palette = listOf(RegistryKey.parse("minecraft:plains")),
        packedData = longArrayOf(0),
    )

    override fun toChunk(position: ChunkPos, records: Map<ByteArray, ByteArray>): Chunk {
        val data2DPayload = records.entries
            .firstOrNull { (key, _) -> BedrockChunkRecordKey.isData2d(key, dimension) }
            ?.value
        val chunkVersion = records.entries
            .firstOrNull { (key, _) -> BedrockChunkRecordKey.isVersion(key, dimension) }
            ?.let { (_, value) -> BedrockChunkVersionDecoder.decode(value) }
        val finalizedState = records.entries
            .firstOrNull { (key, _) -> BedrockChunkRecordKey.isFinalizedState(key, dimension) }
            ?.let { (_, value) -> BedrockFinalizedStateDecoder.decode(value) }
        val biomes = data2DPayload?.let(data2DDecoder::decodeBiomes)
        val heightmap = data2DPayload?.let(data2DDecoder::decodeHeightmap)
        val decodedSections = records.entries
            .mapNotNull { (key, value) ->
                BedrockChunkRecordKey.subChunkY(key, dimension)?.let { sectionY ->
                    subChunkDecoder.decode(sectionY, value) ?: emptySection(sectionY)
                }
            }
            .distinctBy(ChunkSection::y)
            .sortedBy(ChunkSection::y)
        val sections = if (biomes == null) {
            decodedSections
        } else {
            val targetSections = decodedSections.ifEmpty { listOf(emptySection(0)) }
            targetSections.map { section -> section.copy(biomes = biomes) }
        }

        return Chunk(
            position = position,
            sections = sections,
            heightmaps = heightmap?.let { mapOf(BedrockData2DDecoder.HEIGHTMAP_KEY to it) }.orEmpty(),
            rawData = decodedRawData(records, chunkVersion, finalizedState),
        )
    }

    override fun toRecords(chunk: Chunk): Map<ByteArray, ByteArray> {
        val records = LinkedHashMap<ByteArray, ByteArray>().apply {
            putAll(
                BedrockChunkRawPayload.decode(chunk.rawData[BedrockChunkRawPayload.RAW_DATA_KEY])
                    .filterNot { (key, _) -> isManagedRecord(key) },
            )
        }
        val chunkVersion = (chunk.rawData[BedrockChunkVersionDecoder.RAW_DATA_KEY] as? NbtValue.IntValue)?.value
            ?: DEFAULT_CHUNK_VERSION
        records.replaceRecord(
            key = BedrockChunkRecordKey.version(chunk.position, dimension).encode(),
            value = BedrockChunkVersionDecoder.encode(chunkVersion),
        )
        val finalizedState = (chunk.rawData[BedrockFinalizedStateDecoder.RAW_DATA_KEY] as? NbtValue.IntValue)?.value
        if (finalizedState != null) {
            records.replaceRecord(
                key = BedrockChunkRecordKey.finalizedState(chunk.position, dimension).encode(),
                value = BedrockFinalizedStateDecoder.encode(finalizedState),
            )
        }
        val heightmap = chunk.heightmaps[BedrockData2DDecoder.HEIGHTMAP_KEY]
        val biomes = chunk.sections.firstOrNull()?.biomes
        if (heightmap != null || biomes != null) {
            records.replaceRecord(
                key = BedrockChunkRecordKey.data2d(chunk.position, dimension).encode(),
                value = data2DDecoder.encode(heightmap, biomes),
            )
        }
        chunk.sections.forEach { section ->
            records.replaceRecord(
                key = BedrockChunkRecordKey.subChunk(chunk.position, section.y, dimension).encode(),
                value = subChunkDecoder.encode(section),
            )
        }
        return records
    }

    private fun isManagedRecord(key: ByteArray): Boolean =
        BedrockChunkRecordKey.isVersion(key, dimension) ||
            BedrockChunkRecordKey.isData2d(key, dimension) ||
            BedrockChunkRecordKey.isFinalizedState(key, dimension) ||
            BedrockChunkRecordKey.subChunkY(key, dimension) != null

    private fun emptySection(y: Int): ChunkSection =
        ChunkSection(
            y = y,
            blocks = defaultBlockPalette,
            biomes = defaultBiomePalette,
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )

    private fun decodedRawData(
        records: Map<ByteArray, ByteArray>,
        chunkVersion: Int?,
        finalizedState: Int?,
    ): Map<String, NbtValue> {
        val rawData = linkedMapOf<String, NbtValue>(
            BedrockChunkRawPayload.RAW_DATA_KEY to BedrockChunkRawPayload.encode(records),
        )
        if (chunkVersion != null) {
            rawData[BedrockChunkVersionDecoder.RAW_DATA_KEY] = NbtValue.IntValue(chunkVersion)
        }
        if (finalizedState != null) {
            rawData[BedrockFinalizedStateDecoder.RAW_DATA_KEY] = NbtValue.IntValue(finalizedState)
        }
        return rawData
    }

    private fun LinkedHashMap<ByteArray, ByteArray>.replaceRecord(key: ByteArray, value: ByteArray) {
        val existingKey = keys.firstOrNull { it.contentEquals(key) }
        if (existingKey != null) {
            remove(existingKey)
        }
        put(key, value)
    }

    private companion object {
        const val DEFAULT_CHUNK_VERSION = 8
    }
}
