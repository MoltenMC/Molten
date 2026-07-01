package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.nbt.NbtValue
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
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
        assertEquals(2, records.size)
        assertContentEquals(value, records.entries.first { it.key.contentEquals(key) }.value)
        assertContentEquals(
            byteArrayOf(8),
            records.entries.first { it.key.contentEquals(BedrockChunkRecordKey.version(ChunkPos(1, 2)).encode()) }.value,
        )
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
    fun appliesData2dBiomesToDecodedSubChunkSections() {
        val position = ChunkPos(1, 2)
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 0).encode()
        val data2dKey = BedrockChunkRecordKey.data2d(position).encode()
        val data2dPayload = data2dPayload(
            biomes = mapOf(0 to 1, 1 to 4),
        )

        val chunk = mapper.toChunk(
            position,
            mapOf(
                subChunkKey to byteArrayOf(8, 0, 1),
                data2dKey to data2dPayload,
            ),
        )

        val biomes = chunk.sections.single().biomes
        assertEquals(
            listOf(RegistryKey.parse("minecraft:plains"), RegistryKey.molten("unknown_bedrock_biome_4")),
            biomes.palette,
        )
        assertEquals(RegistryKey.parse("minecraft:plains"), biomes.valueAt(0))
        assertEquals(RegistryKey.molten("unknown_bedrock_biome_4"), biomes.valueAt(1))
    }

    @Test
    fun createsSectionSkeletonWhenOnlyData2dBiomeRecordExists() {
        val position = ChunkPos(1, 2)
        val data2dKey = BedrockChunkRecordKey.data2d(position).encode()
        val data2dPayload = data2dPayload()

        val chunk = mapper.toChunk(position, mapOf(data2dKey to data2dPayload))

        val section = chunk.sections.single()
        assertEquals(0, section.y)
        assertEquals(RegistryKey.parse("minecraft:plains"), section.biomes.palette.single())
    }

    @Test
    fun decodesData2dHeightmapIntoChunkHeightmaps() {
        val position = ChunkPos(1, 2)
        val data2dKey = BedrockChunkRecordKey.data2d(position).encode()
        val data2dPayload = data2dPayload(
            heightmap = mapOf(0 to 64, 1 to 255, 255 to 320),
        )

        val chunk = mapper.toChunk(position, mapOf(data2dKey to data2dPayload))

        val heightmap = chunk.heightmaps.getValue(BedrockData2DDecoder.HEIGHTMAP_KEY)
        assertEquals(256, heightmap.size)
        assertEquals(64, heightmap[0])
        assertEquals(255, heightmap[1])
        assertEquals(320, heightmap[255])
    }

    @Test
    fun decodesFinalizedStateRecordIntoChunkRawData() {
        val position = ChunkPos(1, 2)
        val finalizedStateKey = BedrockChunkRecordKey.finalizedState(position).encode()
        val payload = byteArrayOf(3, 0, 0, 0)

        val chunk = mapper.toChunk(position, mapOf(finalizedStateKey to payload))

        assertEquals(
            NbtValue.IntValue(3),
            chunk.rawData[BedrockFinalizedStateDecoder.RAW_DATA_KEY],
        )
    }

    @Test
    fun decodesChunkVersionRecordIntoChunkRawData() {
        val position = ChunkPos(1, 2)
        val versionKey = BedrockChunkRecordKey.version(position).encode()

        val chunk = mapper.toChunk(position, mapOf(versionKey to byteArrayOf(40)))

        assertEquals(
            NbtValue.IntValue(40),
            chunk.rawData[BedrockChunkVersionDecoder.RAW_DATA_KEY],
        )
    }

    @Test
    fun decodesSingleByteFinalizedStateRecordIntoChunkRawData() {
        val position = ChunkPos(1, 2)
        val finalizedStateKey = BedrockChunkRecordKey.finalizedState(position).encode()

        val chunk = mapper.toChunk(position, mapOf(finalizedStateKey to byteArrayOf(5)))

        assertEquals(
            NbtValue.IntValue(5),
            chunk.rawData[BedrockFinalizedStateDecoder.RAW_DATA_KEY],
        )
    }

    @Test
    fun writesDecodedFinalizedStateBackToRecords() {
        val position = ChunkPos(1, 2)
        val finalizedStateKey = BedrockChunkRecordKey.finalizedState(position).encode()
        val chunk = mapper.toChunk(position, mapOf(finalizedStateKey to byteArrayOf(3, 0, 0, 0)))
            .copy(
                rawData = mapOf(
                    BedrockChunkRawPayload.RAW_DATA_KEY to chunkRawRecords(position),
                    BedrockFinalizedStateDecoder.RAW_DATA_KEY to NbtValue.IntValue(7),
                ),
            )

        val records = mapper.toRecords(chunk)

        assertContentEquals(
            byteArrayOf(7, 0, 0, 0),
            records.entries.first { it.key.contentEquals(finalizedStateKey) }.value,
        )
    }

    @Test
    fun writesDecodedChunkVersionBackToRecords() {
        val position = ChunkPos(1, 2)
        val versionKey = BedrockChunkRecordKey.version(position).encode()
        val chunk = mapper.toChunk(position, mapOf(versionKey to byteArrayOf(39)))
            .copy(
                rawData = mapOf(
                    BedrockChunkRawPayload.RAW_DATA_KEY to BedrockChunkRawPayload.encode(
                        mapOf(versionKey to byteArrayOf(39)),
                    ),
                    BedrockChunkVersionDecoder.RAW_DATA_KEY to NbtValue.IntValue(40),
                ),
            )

        val records = mapper.toRecords(chunk)

        assertContentEquals(
            byteArrayOf(40),
            records.entries.first { it.key.contentEquals(versionKey) }.value,
        )
    }

    @Test
    fun writesNonOverworldRecordKeysWhenConfiguredForDimension() {
        val position = ChunkPos(1, 2)
        val dimension = 1
        val dimensionMapper = RawPreservingLevelDbChunkMapper(dimension = dimension)
        val chunk = dimensionMapper.toChunk(position, emptyMap())
            .copy(
                sections = listOf(sectionWithBlocks(BlockState(RegistryKey.parse("minecraft:stone")))),
                rawData = mapOf(BedrockChunkRawPayload.RAW_DATA_KEY to BedrockChunkRawPayload.encode(emptyMap())),
            )

        val records = dimensionMapper.toRecords(chunk)

        assertContentEquals(
            byteArrayOf(8),
            records.entries.first {
                it.key.contentEquals(BedrockChunkRecordKey.version(position, dimension).encode())
            }.value,
        )
        assertEquals(
            1,
            records.keys.count { key ->
                key.contentEquals(BedrockChunkRecordKey.subChunk(position, subChunkY = 0, dimension).encode())
            },
        )
    }

    @Test
    fun ignoresOverworldRecordTypesWhenConfiguredForNonOverworldDimension() {
        val position = ChunkPos(1, 2)
        val dimensionMapper = RawPreservingLevelDbChunkMapper(dimension = 1)
        val overworldVersionKey = BedrockChunkRecordKey.version(position).encode()
        val overworldSubChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 0).encode()

        val chunk = dimensionMapper.toChunk(
            position,
            mapOf(
                overworldVersionKey to byteArrayOf(40),
                overworldSubChunkKey to byteArrayOf(8, 0, 1),
            ),
        )

        assertEquals(emptyList(), chunk.sections)
        assertEquals(null, chunk.rawData[BedrockChunkVersionDecoder.RAW_DATA_KEY])
    }

    @Test
    fun dropsStaleManagedRecordsFromRawPayloadWhenWritingRecords() {
        val position = ChunkPos(1, 2)
        val versionKey = BedrockChunkRecordKey.version(position).encode()
        val data2dKey = BedrockChunkRecordKey.data2d(position).encode()
        val finalizedStateKey = BedrockChunkRecordKey.finalizedState(position).encode()
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 0).encode()
        val legacyTerrainKey = BedrockChunkRecordKey(
            position = position,
            dimension = BedrockChunkRecordKey.OVERWORLD_DIMENSION,
            tag = BedrockChunkRecordKey.LEGACY_TERRAIN_TAG,
        ).encode()
        val chunk = mapper.toChunk(
            position,
            mapOf(
                versionKey to byteArrayOf(40),
                data2dKey to data2dPayload(),
                finalizedStateKey to byteArrayOf(3, 0, 0, 0),
                subChunkKey to byteArrayOf(8, 0, 1),
                legacyTerrainKey to byteArrayOf(7, 8, 9),
            ),
        ).copy(
            sections = emptyList(),
            heightmaps = emptyMap(),
            rawData = mapOf(BedrockChunkRawPayload.RAW_DATA_KEY to chunkRawRecords(position)),
        )

        val records = mapper.toRecords(chunk)

        assertEquals(2, records.size)
        assertContentEquals(byteArrayOf(8), records.entries.first { it.key.contentEquals(versionKey) }.value)
        assertContentEquals(byteArrayOf(7, 8, 9), records.entries.first { it.key.contentEquals(legacyTerrainKey) }.value)
    }

    @Test
    fun writesSubChunkBlocksBackToRecords() {
        val position = ChunkPos(1, 2)
        val subChunkKey = BedrockChunkRecordKey.subChunk(position, subChunkY = 0).encode()
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val mapper = RawPreservingLevelDbChunkMapper(
            subChunkDecoder = BedrockSubChunkDecoder(
                runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone)),
            ),
        )
        val chunk = mapper.toChunk(position, emptyMap())
            .copy(
                sections = listOf(sectionWithBlocks(stone)),
                rawData = mapOf(BedrockChunkRawPayload.RAW_DATA_KEY to BedrockChunkRawPayload.encode(emptyMap())),
            )

        val records = mapper.toRecords(chunk)
        val payload = records.entries.first { it.key.contentEquals(subChunkKey) }.value
        assertContentEquals(byteArrayOf(8, 1, 1, 1, 1), payload)
        val decoded = BedrockSubChunkDecoder(
            runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone)),
        ).decode(0, payload)

        assertEquals(stone, decoded?.blocks?.palette?.single())
    }

    private fun data2dPayload(
        heightmap: Map<Int, Int> = emptyMap(),
        biomes: Map<Int, Int> = emptyMap(),
    ): ByteArray {
        val payload = ByteArray(512 + 256)
        repeat(256) { index ->
            val height = heightmap[index] ?: 0
            val offset = index * 2
            payload[offset] = (height and 0xff).toByte()
            payload[offset + 1] = ((height ushr 8) and 0xff).toByte()
            payload[512 + index] = (biomes[index] ?: 1).toByte()
        }
        return payload
    }

    private fun chunkRawRecords(position: ChunkPos): NbtValue =
        BedrockChunkRawPayload.encode(
            mapOf(
                BedrockChunkRecordKey.finalizedState(position).encode() to byteArrayOf(3, 0, 0, 0),
                BedrockChunkRecordKey(
                    position = position,
                    dimension = BedrockChunkRecordKey.OVERWORLD_DIMENSION,
                    tag = BedrockChunkRecordKey.LEGACY_TERRAIN_TAG,
                ).encode() to byteArrayOf(7, 8, 9),
            ),
        )

    private fun sectionWithBlocks(primary: BlockState): ChunkSection =
        ChunkSection(
            y = 0,
            blocks = PalettedContainer(
                palette = listOf(primary),
                packedData = longArrayOf(0),
            ),
            biomes = PalettedContainer(
                palette = listOf(RegistryKey.parse("minecraft:plains")),
                packedData = longArrayOf(0),
            ),
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )
}
