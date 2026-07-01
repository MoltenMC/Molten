package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import io.github.moltenmc.molten.translator.biome.BedrockBiomeIdTranslator
import io.github.moltenmc.molten.translator.biome.MapBackedBedrockBiomeIdTranslator
import kotlin.math.ceil
import kotlin.math.log2

class BedrockData2DDecoder(
    private val biomeIdTranslator: BedrockBiomeIdTranslator = MapBackedBedrockBiomeIdTranslator(
        mapOf(PLAINS_BIOME_ID to RegistryKey.parse("minecraft:plains")),
    ),
) {
    fun encode(
        heightmap: LongArray?,
        biomes: PalettedContainer<RegistryKey>?,
    ): ByteArray {
        val payload = ByteArray(HEIGHTMAP_BYTES + BIOME_BYTES)
        val safeHeightmap = heightmap ?: LongArray(HEIGHTMAP_ENTRIES)
        repeat(HEIGHTMAP_ENTRIES) { index ->
            val height = safeHeightmap.getOrElse(index) { 0L }.coerceIn(0L, 0xffffL).toInt()
            val offset = index * Short.SIZE_BYTES
            payload[offset] = (height and 0xff).toByte()
            payload[offset + 1] = ((height ushr Byte.SIZE_BITS) and 0xff).toByte()
        }

        repeat(BIOME_BYTES) { index ->
            val key = biomes?.valueAt(index) ?: RegistryKey.parse("minecraft:plains")
            val biomeId = biomeIdTranslator.toBedrockBiomeId(key) ?: PLAINS_BIOME_ID
            require(biomeId in 0..0xff) { "Bedrock Data2D biome ID $biomeId is outside byte range." }
            payload[HEIGHTMAP_BYTES + index] = (biomeId and 0xff).toByte()
        }
        return payload
    }

    fun decodeHeightmap(payload: ByteArray): LongArray? {
        if (payload.size < HEIGHTMAP_BYTES) {
            return null
        }

        return LongArray(HEIGHTMAP_ENTRIES) { index ->
            val offset = index * Short.SIZE_BYTES
            ((payload[offset].toInt() and 0xff) or
                ((payload[offset + 1].toInt() and 0xff) shl Byte.SIZE_BITS)).toLong()
        }
    }

    fun decodeBiomes(payload: ByteArray): PalettedContainer<RegistryKey>? {
        if (payload.size < HEIGHTMAP_BYTES + BIOME_BYTES) {
            return null
        }

        val paletteIndexes = IntArray(BIOME_BYTES)
        val palette = ArrayList<RegistryKey>()
        val paletteIndexByKey = LinkedHashMap<RegistryKey, Int>()

        repeat(BIOME_BYTES) { index ->
            val biomeId = payload[HEIGHTMAP_BYTES + index].toInt() and 0xff
            val key = biomeIdTranslator.toInternalBiomeKey(biomeId) ?: unknownBiomeKey(biomeId)
            val paletteIndex = paletteIndexByKey.getOrPut(key) {
                palette += key
                palette.lastIndex
            }
            paletteIndexes[index] = paletteIndex
        }

        return PalettedContainer(
            palette = palette,
            packedData = packPaletteIndexes(paletteIndexes, bitsPerEntry(palette.size)),
            bitsPerEntry = bitsPerEntry(palette.size),
            wordBits = Long.SIZE_BITS,
        )
    }

    private fun bitsPerEntry(paletteSize: Int): Int {
        if (paletteSize <= 1) {
            return 0
        }
        return ceil(log2(paletteSize.toDouble())).toInt().coerceAtLeast(1)
    }

    private fun packPaletteIndexes(indexes: IntArray, bitsPerEntry: Int): LongArray {
        if (bitsPerEntry == 0) {
            return LongArray(0)
        }
        val entriesPerWord = Long.SIZE_BITS / bitsPerEntry
        val words = LongArray((indexes.size + entriesPerWord - 1) / entriesPerWord)
        val mask = (1L shl bitsPerEntry) - 1L
        indexes.forEachIndexed { index, paletteIndex ->
            val wordIndex = index / entriesPerWord
            val bitOffset = (index % entriesPerWord) * bitsPerEntry
            words[wordIndex] = words[wordIndex] or ((paletteIndex.toLong() and mask) shl bitOffset)
        }
        return words
    }

    private fun unknownBiomeKey(biomeId: Int): RegistryKey =
        RegistryKey.molten("unknown_bedrock_biome_$biomeId")

    companion object {
        const val HEIGHTMAP_KEY = "bedrock:heightmap"
        private const val HEIGHTMAP_ENTRIES = 16 * 16
        private const val HEIGHTMAP_BYTES = 16 * 16 * Short.SIZE_BYTES
        private const val BIOME_BYTES = 16 * 16
        private const val PLAINS_BIOME_ID = 1
    }
}
