package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeIdTranslator
import io.github.moltenmc.molten.translator.block.MapBackedBedrockBlockRuntimeIdTranslator

class BedrockSubChunkDecoder(
    private val runtimeIdTranslator: BedrockBlockRuntimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(
        mapOf(0 to BlockState(AIR)),
    ),
) {
    fun decode(sectionY: Int, payload: ByteArray): ChunkSection? {
        val reader = PayloadReader(payload)
        val version = reader.readUnsignedByteOrNull() ?: return null
        if (version !in SUPPORTED_VERSIONS) {
            return null
        }
        val storageCount = reader.readUnsignedByteOrNull() ?: return null
        if (storageCount <= 0) {
            return emptySection(sectionY)
        }

        val blocks = readBlockStorage(reader) ?: return null
        return ChunkSection(
            y = sectionY,
            blocks = blocks,
            biomes = emptyBiomePalette,
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )
    }

    private fun readBlockStorage(reader: PayloadReader): PalettedContainer<BlockState>? {
        val header = reader.readUnsignedByteOrNull() ?: return null
        val bitsPerBlock = header ushr 1
        val runtimePalette = (header and 1) == 1
        val packedData = reader.readPackedWords(bitsPerBlock) ?: return null

        val paletteSize = reader.readUnsignedVarIntOrNull() ?: return null
        if (!runtimePalette) {
            return null
        }

        val palette = List(paletteSize) {
            val runtimeId = reader.readUnsignedVarIntOrNull() ?: return null
            runtimeIdTranslator.toInternalBlockState(runtimeId) ?: unknownBlockState(runtimeId)
        }
        return PalettedContainer(
            palette = palette.ifEmpty { listOf(BlockState(AIR)) },
            packedData = packedData,
        )
    }

    private fun emptySection(sectionY: Int): ChunkSection =
        ChunkSection(
            y = sectionY,
            blocks = PalettedContainer(
                palette = listOf(BlockState(AIR)),
                packedData = longArrayOf(0),
            ),
            biomes = emptyBiomePalette,
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
        )

    private fun unknownBlockState(runtimeId: Int): BlockState =
        BlockState(
            key = UNKNOWN_BEDROCK_BLOCK,
            properties = mapOf("runtime_id" to runtimeId.toString()),
        )

    private fun PayloadReader.readPackedWords(bitsPerBlock: Int): LongArray? {
        if (bitsPerBlock == 0) {
            return LongArray(0)
        }
        val valuesPerWord = 32 / bitsPerBlock
        if (valuesPerWord <= 0) {
            return null
        }
        val wordCount = (BLOCKS_PER_SECTION + valuesPerWord - 1) / valuesPerWord
        return LongArray(wordCount) {
            readLittleEndianIntOrNull()?.toLong()?.and(0xffff_ffffL) ?: return null
        }
    }

    private class PayloadReader(private val payload: ByteArray) {
        private var offset = 0

        fun readUnsignedByteOrNull(): Int? {
            if (offset >= payload.size) {
                return null
            }
            return payload[offset++].toInt() and 0xff
        }

        fun readLittleEndianIntOrNull(): Int? {
            if (offset + 4 > payload.size) {
                return null
            }
            val value = (payload[offset].toInt() and 0xff) or
                ((payload[offset + 1].toInt() and 0xff) shl 8) or
                ((payload[offset + 2].toInt() and 0xff) shl 16) or
                ((payload[offset + 3].toInt() and 0xff) shl 24)
            offset += 4
            return value
        }

        fun readUnsignedVarIntOrNull(): Int? {
            var value = 0
            var shift = 0
            repeat(MAX_VAR_INT_BYTES) {
                val byte = readUnsignedByteOrNull() ?: return null
                value = value or ((byte and 0x7f) shl shift)
                if (byte and 0x80 == 0) {
                    return value
                }
                shift += 7
            }
            return null
        }
    }

    private companion object {
        const val BLOCKS_PER_SECTION = 16 * 16 * 16
        const val MAX_VAR_INT_BYTES = 5
        val SUPPORTED_VERSIONS = setOf(8, 9)
        val AIR = RegistryKey.parse("minecraft:air")
        val UNKNOWN_BEDROCK_BLOCK = RegistryKey.molten("unknown_bedrock_block")
        val emptyBiomePalette = PalettedContainer(
            palette = listOf(RegistryKey.parse("minecraft:plains")),
            packedData = longArrayOf(0),
        )
    }
}
