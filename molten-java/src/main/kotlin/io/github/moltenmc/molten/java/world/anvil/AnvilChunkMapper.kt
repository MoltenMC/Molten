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

interface AnvilChunkMapper {
    fun toChunk(position: ChunkPos, rawChunkData: ByteArray): Chunk

    fun toRawChunkData(chunk: Chunk): ByteArray
}

class DefaultAnvilChunkMapper : AnvilChunkMapper {
    override fun toChunk(position: ChunkPos, rawChunkData: ByteArray): Chunk {
        val root = NbtCodec.decode(rawChunkData, NbtFormat.JAVA)
        return Chunk(
            position = position,
            sections = root.compoundList("sections").map(::sectionFromNbt),
            dataVersion = root.int("DataVersion"),
            status = root.string("Status") ?: DEFAULT_STATUS,
            inhabitedTime = root.long("InhabitedTime"),
            heightmaps = root.compound("Heightmaps")
                ?.values
                .orEmpty()
                .mapNotNull { (key, value) -> (value as? NbtValue.LongArrayValue)?.let { key to it.value } }
                .toMap(),
            blockEntities = root.compoundList("block_entities").mapNotNull(::blockEntityFromNbt),
            rawData = root.values.filterKeys { it !in KNOWN_ROOT_KEYS },
        )
    }

    override fun toRawChunkData(chunk: Chunk): ByteArray {
        val rootValues = LinkedHashMap<String, NbtValue>().apply {
            putAll(chunk.rawData.filterKeys { it !in KNOWN_ROOT_KEYS })
        }
        rootValues += linkedMapOf<String, NbtValue>(
                "DataVersion" to NbtValue.IntValue(chunk.dataVersion),
                "xPos" to NbtValue.IntValue(chunk.position.x),
                "zPos" to NbtValue.IntValue(chunk.position.z),
                "Status" to NbtValue.StringValue(chunk.status),
                "InhabitedTime" to NbtValue.LongValue(chunk.inhabitedTime),
                "sections" to NbtValue.ListValue(chunk.sections.map(::sectionToNbt)),
        )
        if (chunk.heightmaps.isNotEmpty()) {
            rootValues["Heightmaps"] = NbtValue.CompoundValue(
                chunk.heightmaps.toSortedMap().mapValues { (_, value) -> NbtValue.LongArrayValue(value) },
            )
        }
        if (chunk.blockEntities.isNotEmpty()) {
            rootValues["block_entities"] = NbtValue.ListValue(chunk.blockEntities.map(BlockEntityData::rawNbt))
        }
        val root = NbtValue.CompoundValue(rootValues)
        return NbtCodec.encode(root, NbtFormat.JAVA, ROOT_NAME)
    }

    private fun sectionFromNbt(section: NbtValue.CompoundValue): ChunkSection =
        ChunkSection(
            y = section.byte("Y").toInt(),
            blocks = section.compound("block_states")?.let(::blockPalettedContainerFromNbt) ?: emptyBlocks(),
            biomes = section.compound("biomes")?.let(::registryPalettedContainerFromNbt) ?: emptyBiomes(),
            light = LightData(
                blockLight = section.byteArray("BlockLight"),
                skyLight = section.byteArray("SkyLight"),
            ),
        )

    private fun sectionToNbt(section: ChunkSection): NbtValue.CompoundValue =
        NbtValue.CompoundValue(
            mapOf(
                "Y" to NbtValue.ByteValue(section.y.toByte()),
                "block_states" to blockPalettedContainerToNbt(section.blocks),
                "biomes" to registryPalettedContainerToNbt(section.biomes),
                "BlockLight" to NbtValue.ByteArrayValue(section.light.blockLight),
                "SkyLight" to NbtValue.ByteArrayValue(section.light.skyLight),
            ),
        )

    private fun blockPalettedContainerFromNbt(value: NbtValue.CompoundValue): PalettedContainer<BlockState> {
        val palette = value.compoundList("palette")
            .mapNotNull(::blockStateFromPaletteEntry)
            .ifEmpty { listOf(BlockState(AIR)) }
        return PalettedContainer(
            palette = palette,
            packedData = value.longArray("data"),
            bitsPerEntry = bitsPerEntry(palette.size, minBits = 4),
            wordBits = Long.SIZE_BITS,
        )
    }

    private fun registryPalettedContainerFromNbt(value: NbtValue.CompoundValue): PalettedContainer<RegistryKey> {
        val palette = value.compoundList("palette")
            .mapNotNull { it.string("Name") }
            .map(RegistryKey::parse)
            .ifEmpty { listOf(PLAINS) }
        return PalettedContainer(
            palette = palette,
            packedData = value.longArray("data"),
            bitsPerEntry = bitsPerEntry(palette.size, minBits = 1),
            wordBits = Long.SIZE_BITS,
        )
    }

    private fun blockStateFromPaletteEntry(entry: NbtValue.CompoundValue): BlockState? {
        val name = entry.string("Name") ?: return null
        return BlockState(
            key = RegistryKey.parse(name),
            properties = entry.compound("Properties")
                ?.values
                .orEmpty()
                .mapValues { (_, value) -> (value as? NbtValue.StringValue)?.value.orEmpty() }
                .filterValues(String::isNotEmpty),
        )
    }

    private fun blockPalettedContainerToNbt(container: PalettedContainer<BlockState>): NbtValue.CompoundValue =
        NbtValue.CompoundValue(
            mapOf(
                "palette" to NbtValue.ListValue(
                    container.palette.map(::blockStateToPaletteEntry),
                ),
                "data" to NbtValue.LongArrayValue(container.packedData),
            ),
        )

    private fun registryPalettedContainerToNbt(container: PalettedContainer<RegistryKey>): NbtValue.CompoundValue =
        NbtValue.CompoundValue(
            mapOf(
                "palette" to NbtValue.ListValue(
                    container.palette.map { key ->
                        NbtValue.CompoundValue(
                            mapOf("Name" to NbtValue.StringValue(key.toString())),
                        )
                    },
                ),
                "data" to NbtValue.LongArrayValue(container.packedData),
            ),
        )

    private fun blockStateToPaletteEntry(blockState: BlockState): NbtValue.CompoundValue {
        val values = linkedMapOf<String, NbtValue>(
            "Name" to NbtValue.StringValue(blockState.key.toString()),
        )
        if (blockState.properties.isNotEmpty()) {
            values["Properties"] = NbtValue.CompoundValue(
                blockState.properties.toSortedMap().mapValues { (_, value) -> NbtValue.StringValue(value) },
            )
        }
        return NbtValue.CompoundValue(values)
    }

    private fun blockEntityFromNbt(value: NbtValue.CompoundValue): BlockEntityData? {
        val type = value.string("id")?.let(RegistryKey::parse) ?: return null
        return BlockEntityData(
            position = BlockPos(
                x = value.int("x"),
                y = value.int("y"),
                z = value.int("z"),
            ),
            type = type,
            rawNbt = value,
        )
    }

    private fun NbtValue.CompoundValue.compound(key: String): NbtValue.CompoundValue? =
        values[key] as? NbtValue.CompoundValue

    private fun NbtValue.CompoundValue.compoundList(key: String): List<NbtValue.CompoundValue> =
        ((values[key] as? NbtValue.ListValue)?.values).orEmpty().filterIsInstance<NbtValue.CompoundValue>()

    private fun NbtValue.CompoundValue.byte(key: String): Byte =
        (values[key] as? NbtValue.ByteValue)?.value ?: 0

    private fun NbtValue.CompoundValue.int(key: String): Int =
        (values[key] as? NbtValue.IntValue)?.value ?: 0

    private fun NbtValue.CompoundValue.long(key: String): Long =
        (values[key] as? NbtValue.LongValue)?.value ?: 0

    private fun NbtValue.CompoundValue.string(key: String): String? =
        (values[key] as? NbtValue.StringValue)?.value

    private fun NbtValue.CompoundValue.byteArray(key: String): ByteArray =
        (values[key] as? NbtValue.ByteArrayValue)?.value ?: ByteArray(0)

    private fun NbtValue.CompoundValue.longArray(key: String): LongArray =
        (values[key] as? NbtValue.LongArrayValue)?.value ?: LongArray(0)

    private fun bitsPerEntry(paletteSize: Int, minBits: Int): Int {
        if (paletteSize <= 1) {
            return 0
        }
        var bits = minBits
        while ((1 shl bits) < paletteSize) {
            bits++
        }
        return bits
    }

    private fun emptyBlocks(): PalettedContainer<BlockState> =
        PalettedContainer(palette = listOf(BlockState(AIR)), packedData = LongArray(0))

    private fun emptyBiomes(): PalettedContainer<RegistryKey> =
        PalettedContainer(palette = listOf(PLAINS), packedData = LongArray(0))

    private companion object {
        const val ROOT_NAME = ""
        const val DEFAULT_STATUS = "minecraft:empty"
        val AIR = RegistryKey.parse("minecraft:air")
        val PLAINS = RegistryKey.parse("minecraft:plains")
        val KNOWN_ROOT_KEYS = setOf(
            "DataVersion",
            "xPos",
            "zPos",
            "Status",
            "InhabitedTime",
            "Heightmaps",
            "sections",
            "block_entities",
        )
    }
}
