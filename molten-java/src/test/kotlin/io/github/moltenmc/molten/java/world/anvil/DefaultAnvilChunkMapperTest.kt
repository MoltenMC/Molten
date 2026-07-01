package io.github.moltenmc.molten.java.world.anvil

import io.github.moltenmc.molten.common.nbt.NbtCodec
import io.github.moltenmc.molten.common.nbt.NbtFormat
import io.github.moltenmc.molten.common.nbt.NbtValue
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.BlockPos
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.BlockEntityData
import io.github.moltenmc.molten.common.world.chunk.Chunk
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import kotlin.test.assertContentEquals
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAnvilChunkMapperTest {
    private val mapper = DefaultAnvilChunkMapper()

    @Test
    fun mapsRawJavaNbtToChunkSkeleton() {
        val raw = NbtCodec.encode(
            NbtValue.CompoundValue(
                mapOf(
                    "DataVersion" to NbtValue.IntValue(3953),
                    "xPos" to NbtValue.IntValue(4),
                    "zPos" to NbtValue.IntValue(8),
                    "Status" to NbtValue.StringValue("minecraft:full"),
                    "InhabitedTime" to NbtValue.LongValue(12345L),
                    "Heightmaps" to NbtValue.CompoundValue(
                        mapOf(
                            "MOTION_BLOCKING" to NbtValue.LongArrayValue(longArrayOf(10L, 20L)),
                            "WORLD_SURFACE" to NbtValue.LongArrayValue(longArrayOf(30L)),
                        ),
                    ),
                    "entities" to NbtValue.ListValue(
                        listOf(
                            NbtValue.CompoundValue(
                                mapOf("id" to NbtValue.StringValue("minecraft:zombie")),
                            ),
                        ),
                    ),
                    "structures" to NbtValue.CompoundValue(
                        mapOf("starts" to NbtValue.CompoundValue(emptyMap())),
                    ),
                    "molten_custom" to NbtValue.StringValue("preserve-me"),
                    "block_entities" to NbtValue.ListValue(
                        listOf(
                            NbtValue.CompoundValue(
                                mapOf(
                                    "id" to NbtValue.StringValue("minecraft:chest"),
                                    "x" to NbtValue.IntValue(65),
                                    "y" to NbtValue.IntValue(70),
                                    "z" to NbtValue.IntValue(129),
                                    "CustomName" to NbtValue.StringValue("{\"text\":\"Storage\"}"),
                                ),
                            ),
                        ),
                    ),
                    "sections" to NbtValue.ListValue(
                        listOf(
                            NbtValue.CompoundValue(
                                mapOf(
                                    "Y" to NbtValue.ByteValue(0),
                                    "block_states" to NbtValue.CompoundValue(
                                        mapOf(
                                            "palette" to NbtValue.ListValue(
                                                listOf(
                                                    NbtValue.CompoundValue(
                                                        mapOf(
                                                            "Name" to NbtValue.StringValue("minecraft:oak_log"),
                                                            "Properties" to NbtValue.CompoundValue(
                                                                mapOf("axis" to NbtValue.StringValue("y")),
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                            "data" to NbtValue.LongArrayValue(longArrayOf(0L, 1L)),
                                        ),
                                    ),
                                    "biomes" to NbtValue.CompoundValue(
                                        mapOf(
                                            "palette" to NbtValue.ListValue(
                                                listOf(
                                                    NbtValue.CompoundValue(
                                                        mapOf("Name" to NbtValue.StringValue("minecraft:plains")),
                                                    ),
                                                ),
                                            ),
                                            "data" to NbtValue.LongArrayValue(longArrayOf(2L)),
                                        ),
                                    ),
                                    "BlockLight" to NbtValue.ByteArrayValue(byteArrayOf(1, 2)),
                                    "SkyLight" to NbtValue.ByteArrayValue(byteArrayOf(3, 4)),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            NbtFormat.JAVA,
        )

        val chunk = mapper.toChunk(ChunkPos(4, 8), raw)

        assertEquals(ChunkPos(4, 8), chunk.position)
        assertEquals(3953, chunk.dataVersion)
        assertEquals("minecraft:full", chunk.status)
        assertEquals(12345L, chunk.inhabitedTime)
        assertContentEquals(longArrayOf(10L, 20L), chunk.heightmaps["MOTION_BLOCKING"])
        assertContentEquals(longArrayOf(30L), chunk.heightmaps["WORLD_SURFACE"])
        assertEquals(1, chunk.blockEntities.size)
        assertEquals(BlockPos(65, 70, 129), chunk.blockEntities.single().position)
        assertEquals(RegistryKey.parse("minecraft:chest"), chunk.blockEntities.single().type)
        assertEquals(
            NbtValue.StringValue("{\"text\":\"Storage\"}"),
            chunk.blockEntities.single().rawNbt.values["CustomName"],
        )
        assertEquals(false, chunk.rawData.containsKey("DataVersion"))
        assertEquals(false, chunk.rawData.containsKey("sections"))
        assertEquals(
            NbtValue.ListValue(
                listOf(
                    NbtValue.CompoundValue(
                        mapOf("id" to NbtValue.StringValue("minecraft:zombie")),
                    ),
                ),
            ),
            chunk.rawData["entities"],
        )
        assertEquals(
            NbtValue.CompoundValue(mapOf("starts" to NbtValue.CompoundValue(emptyMap()))),
            chunk.rawData["structures"],
        )
        assertEquals(NbtValue.StringValue("preserve-me"), chunk.rawData["molten_custom"])
        assertEquals(1, chunk.sections.size)
        assertEquals(0, chunk.sections.single().y)
        assertEquals(
            listOf(
                BlockState(
                    key = RegistryKey.parse("minecraft:oak_log"),
                    properties = mapOf("axis" to "y"),
                ),
            ),
            chunk.sections.single().blocks.palette,
        )
        assertContentEquals(longArrayOf(0L, 1L), chunk.sections.single().blocks.packedData)
        assertEquals(listOf(RegistryKey.parse("minecraft:plains")), chunk.sections.single().biomes.palette)
        assertContentEquals(byteArrayOf(1, 2), chunk.sections.single().light.blockLight)
    }

    @Test
    fun mapsChunkSkeletonToRawJavaNbt() {
        val raw = mapper.toRawChunkData(
            Chunk(
                ChunkPos(7, 9),
                listOf(
                    ChunkSection(
                        y = 1,
                        blocks = PalettedContainer(
                            palette = listOf(
                                BlockState(
                                    key = RegistryKey.parse("minecraft:water"),
                                    properties = mapOf("level" to "0"),
                                ),
                            ),
                            packedData = longArrayOf(5L),
                        ),
                        biomes = PalettedContainer(
                            palette = listOf(RegistryKey.parse("minecraft:forest")),
                            packedData = longArrayOf(6L),
                        ),
                        light = LightData(
                            blockLight = byteArrayOf(7),
                            skyLight = byteArrayOf(8),
                        ),
                    ),
                ),
                dataVersion = 3953,
                status = "minecraft:full",
                inhabitedTime = 98765L,
                heightmaps = mapOf(
                    "MOTION_BLOCKING" to longArrayOf(1L, 2L),
                    "WORLD_SURFACE" to longArrayOf(3L),
                ),
                blockEntities = listOf(
                    BlockEntityData(
                        position = BlockPos(10, 64, 12),
                        type = RegistryKey.parse("minecraft:sign"),
                        rawNbt = NbtValue.CompoundValue(
                            mapOf(
                                "id" to NbtValue.StringValue("minecraft:sign"),
                                "x" to NbtValue.IntValue(10),
                                "y" to NbtValue.IntValue(64),
                                "z" to NbtValue.IntValue(12),
                                "front_text" to NbtValue.CompoundValue(
                                    mapOf("has_glowing_text" to NbtValue.ByteValue(1)),
                                ),
                            ),
                        ),
                    ),
                ),
                rawData = mapOf(
                    "entities" to NbtValue.ListValue(
                        listOf(
                            NbtValue.CompoundValue(
                                mapOf("id" to NbtValue.StringValue("minecraft:sheep")),
                            ),
                        ),
                    ),
                    "structures" to NbtValue.CompoundValue(
                        mapOf("References" to NbtValue.CompoundValue(emptyMap())),
                    ),
                    "Status" to NbtValue.StringValue("minecraft:raw-data-should-not-win"),
                    "custom_root" to NbtValue.IntValue(42),
                ),
            ),
        )
        val decoded = NbtCodec.decode(raw, NbtFormat.JAVA)

        assertEquals(NbtValue.IntValue(3953), decoded.values["DataVersion"])
        assertEquals(NbtValue.IntValue(7), decoded.values["xPos"])
        assertEquals(NbtValue.IntValue(9), decoded.values["zPos"])
        assertEquals(NbtValue.StringValue("minecraft:full"), decoded.values["Status"])
        assertEquals(NbtValue.LongValue(98765L), decoded.values["InhabitedTime"])
        val heightmaps = decoded.values["Heightmaps"] as NbtValue.CompoundValue
        assertEquals(NbtValue.LongArrayValue(longArrayOf(1L, 2L)), heightmaps.values["MOTION_BLOCKING"])
        assertEquals(NbtValue.LongArrayValue(longArrayOf(3L)), heightmaps.values["WORLD_SURFACE"])
        val blockEntities = decoded.values["block_entities"] as NbtValue.ListValue
        val blockEntity = blockEntities.values.single() as NbtValue.CompoundValue
        assertEquals(NbtValue.StringValue("minecraft:sign"), blockEntity.values["id"])
        assertEquals(NbtValue.IntValue(10), blockEntity.values["x"])
        assertEquals(NbtValue.IntValue(64), blockEntity.values["y"])
        assertEquals(NbtValue.IntValue(12), blockEntity.values["z"])
        assertEquals(
            NbtValue.CompoundValue(mapOf("has_glowing_text" to NbtValue.ByteValue(1))),
            blockEntity.values["front_text"],
        )
        assertEquals(NbtValue.StringValue("minecraft:full"), decoded.values["Status"])
        assertEquals(
            NbtValue.ListValue(
                listOf(
                    NbtValue.CompoundValue(
                        mapOf("id" to NbtValue.StringValue("minecraft:sheep")),
                    ),
                ),
            ),
            decoded.values["entities"],
        )
        assertEquals(
            NbtValue.CompoundValue(mapOf("References" to NbtValue.CompoundValue(emptyMap()))),
            decoded.values["structures"],
        )
        assertEquals(NbtValue.IntValue(42), decoded.values["custom_root"])
        val section = ((decoded.values["sections"] as NbtValue.ListValue).values.single() as NbtValue.CompoundValue)
        assertEquals(NbtValue.ByteValue(1), section.values["Y"])

        val blockStates = section.values["block_states"] as NbtValue.CompoundValue
        val blockPalette = blockStates.values["palette"] as NbtValue.ListValue
        val blockPaletteEntry = blockPalette.values.single() as NbtValue.CompoundValue
        assertEquals(NbtValue.StringValue("minecraft:water"), blockPaletteEntry.values["Name"])
        val properties = blockPaletteEntry.values["Properties"] as NbtValue.CompoundValue
        assertEquals(NbtValue.StringValue("0"), properties.values["level"])
        assertEquals(NbtValue.LongArrayValue(longArrayOf(5L)), blockStates.values["data"])
    }

    @Test
    fun omitsEmptyBlockStatePropertiesWhenWritingPaletteEntries() {
        val raw = mapper.toRawChunkData(
            Chunk(
                ChunkPos(1, 2),
                listOf(
                    ChunkSection(
                        y = 0,
                        blocks = PalettedContainer(
                            palette = listOf(BlockState(RegistryKey.parse("minecraft:stone"))),
                            packedData = longArrayOf(0L),
                        ),
                        biomes = PalettedContainer(
                            palette = listOf(RegistryKey.parse("minecraft:plains")),
                            packedData = LongArray(0),
                        ),
                        light = LightData(ByteArray(0), ByteArray(0)),
                    ),
                ),
            ),
        )
        val decoded = NbtCodec.decode(raw, NbtFormat.JAVA)
        val section = ((decoded.values["sections"] as NbtValue.ListValue).values.single() as NbtValue.CompoundValue)
        val blockStates = section.values["block_states"] as NbtValue.CompoundValue
        val blockPalette = blockStates.values["palette"] as NbtValue.ListValue
        val blockPaletteEntry = blockPalette.values.single() as NbtValue.CompoundValue

        assertEquals(NbtValue.StringValue("minecraft:stone"), blockPaletteEntry.values["Name"])
        assertEquals(false, blockPaletteEntry.values.containsKey("Properties"))
    }
}
