package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import io.github.moltenmc.molten.translator.biome.MapBackedBedrockBiomeIdTranslator
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BedrockData2DDecoderTest {
    @Test
    fun encodesHeightmapValues() {
        val decoder = BedrockData2DDecoder()
        val heightmap = LongArray(256).also {
            it[0] = 64
            it[1] = 255
            it[255] = 320
        }

        val payload = decoder.encode(heightmap, biomes = null)

        assertContentEquals(data2dPayload(heightmap = mapOf(0 to 64, 1 to 255, 255 to 320)), payload)
    }

    @Test
    fun encodesBiomePaletteValues() {
        val forest = RegistryKey.parse("minecraft:forest")
        val decoder = BedrockData2DDecoder(
            biomeIdTranslator = MapBackedBedrockBiomeIdTranslator(
                mapOf(1 to RegistryKey.parse("minecraft:plains"), 4 to forest),
            ),
        )

        val payload = decoder.encode(heightmap = null, biomes = singleBiomePalette(forest))

        assertEquals(4, payload[512].toInt() and 0xff)
    }

    @Test
    fun rejectsBiomeIdsOutsideByteRange() {
        val forest = RegistryKey.parse("minecraft:forest")
        val decoder = BedrockData2DDecoder(
            biomeIdTranslator = MapBackedBedrockBiomeIdTranslator(mapOf(300 to forest)),
        )

        assertFailsWith<IllegalArgumentException> {
            decoder.encode(heightmap = null, biomes = singleBiomePalette(forest))
        }
    }

    private fun data2dPayload(heightmap: Map<Int, Int> = emptyMap()): ByteArray {
        val payload = ByteArray(512 + 256)
        repeat(256) { index ->
            val height = heightmap[index] ?: 0
            val offset = index * 2
            payload[offset] = (height and 0xff).toByte()
            payload[offset + 1] = ((height ushr 8) and 0xff).toByte()
            payload[512 + index] = 1
        }
        return payload
    }

    private fun singleBiomePalette(biome: RegistryKey): PalettedContainer<RegistryKey> =
        PalettedContainer(
            palette = listOf(biome),
            packedData = LongArray(0),
        )
}
