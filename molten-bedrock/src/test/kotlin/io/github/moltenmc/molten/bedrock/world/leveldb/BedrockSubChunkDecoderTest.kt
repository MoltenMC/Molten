package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import io.github.moltenmc.molten.translator.block.MapBackedBedrockBlockRuntimeIdTranslator
import kotlin.test.Test
import kotlin.test.assertEquals

class BedrockSubChunkDecoderTest {
    @Test
    fun decodesRuntimePalette() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val decoder = BedrockSubChunkDecoder(
            runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(7 to stone)),
        )

        val section = decoder.decode(3, byteArrayOf(8, 1, 1, 1, 7))

        assertEquals(3, section?.y)
        assertEquals(stone, section?.blocks?.palette?.single())
        assertEquals(0, section?.blocks?.packedData?.size)
    }

    @Test
    fun readsPackedPaletteIndexes() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val dirt = BlockState(RegistryKey.parse("minecraft:dirt"))
        val decoder = BedrockSubChunkDecoder(
            runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone, 3 to dirt)),
        )
        val payload = byteArrayOf(
            8,
            1,
            3,
            2, 0, 0, 0,
        ) + ByteArray(127 * 4) + byteArrayOf(
            2,
            1,
            3,
        )

        val blocks = decoder.decode(3, payload)?.blocks

        assertEquals(1, blocks?.bitsPerEntry)
        assertEquals(Int.SIZE_BITS, blocks?.wordBits)
        assertEquals(stone, blocks?.valueAt(0))
        assertEquals(dirt, blocks?.valueAt(1))
    }

    @Test
    fun preservesAdditionalStorageLayers() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val water = BlockState(RegistryKey.parse("minecraft:water"))
        val decoder = BedrockSubChunkDecoder(
            runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone, 9 to water)),
        )
        val payload = byteArrayOf(
            8,
            2,
            1,
            1,
            1,
            1,
            1,
            9,
        )

        val section = decoder.decode(3, payload)

        assertEquals(stone, section?.blocks?.palette?.single())
        assertEquals(1, section?.extraBlockLayers?.size)
        assertEquals(water, section?.extraBlockLayers?.single()?.palette?.single())
    }

    @Test
    fun usesMatchingPayloadSectionYForVersionNine() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val decoder = BedrockSubChunkDecoder(
            runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone)),
        )

        val section = decoder.decode(-2, byteArrayOf(9, 1, (-2).toByte(), 1, 1, 1))

        assertEquals(-2, section?.y)
        assertEquals(stone, section?.blocks?.palette?.single())
    }

    @Test
    fun rejectsConflictingVersionNinePayloadSectionY() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val decoder = BedrockSubChunkDecoder(
            runtimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone)),
        )

        assertEquals(null, decoder.decode(1, byteArrayOf(9, 1, 2, 1, 1, 1)))
    }

    @Test
    fun repacksLongWordPalettesWhenEncoding() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val dirt = BlockState(RegistryKey.parse("minecraft:dirt"))
        val runtimeTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone, 2 to dirt))
        val decoder = BedrockSubChunkDecoder(runtimeTranslator)
        val packedData = LongArray(64).also { words ->
            words[0] = 1L shl 40
        }
        val section = sectionWithBlocks(
            primary = stone,
            palette = listOf(stone, dirt),
            packedData = packedData,
            bitsPerEntry = 1,
            wordBits = Long.SIZE_BITS,
        )

        val decoded = decoder.decode(0, decoder.encode(section))

        assertEquals(stone, decoded?.blocks?.valueAt(0))
        assertEquals(dirt, decoded?.blocks?.valueAt(40))
    }

    @Test
    fun encodesAdditionalStorageLayers() {
        val stone = BlockState(RegistryKey.parse("minecraft:stone"))
        val water = BlockState(RegistryKey.parse("minecraft:water"))
        val runtimeTranslator = MapBackedBedrockBlockRuntimeIdTranslator(mapOf(1 to stone, 9 to water))
        val decoder = BedrockSubChunkDecoder(runtimeTranslator)

        val decoded = decoder.decode(0, decoder.encode(sectionWithBlocks(stone, secondary = water)))

        assertEquals(stone, decoded?.blocks?.palette?.single())
        assertEquals(water, decoded?.extraBlockLayers?.single()?.palette?.single())
    }

    private fun sectionWithBlocks(
        primary: BlockState,
        secondary: BlockState? = null,
        palette: List<BlockState> = listOf(primary),
        packedData: LongArray = LongArray(0),
        bitsPerEntry: Int = 0,
        wordBits: Int = Long.SIZE_BITS,
    ): ChunkSection =
        ChunkSection(
            y = 0,
            blocks = PalettedContainer(
                palette = palette,
                packedData = packedData,
                bitsPerEntry = bitsPerEntry,
                wordBits = wordBits,
            ),
            biomes = PalettedContainer(
                palette = listOf(RegistryKey.parse("minecraft:plains")),
                packedData = LongArray(0),
            ),
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
            extraBlockLayers = secondary?.let {
                listOf(PalettedContainer(palette = listOf(it), packedData = LongArray(0)))
            }.orEmpty(),
        )
}
