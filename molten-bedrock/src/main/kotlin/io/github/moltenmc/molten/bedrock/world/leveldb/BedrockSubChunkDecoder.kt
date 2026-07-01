package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.common.world.section.ChunkSection
import io.github.moltenmc.molten.common.world.section.LightData
import io.github.moltenmc.molten.common.world.section.PalettedContainer
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeIdTranslator
import io.github.moltenmc.molten.translator.block.MapBackedBedrockBlockRuntimeIdTranslator
import java.io.ByteArrayOutputStream

class BedrockSubChunkDecoder(
    private val runtimeIdTranslator: BedrockBlockRuntimeIdTranslator = MapBackedBedrockBlockRuntimeIdTranslator(
        mapOf(0 to BlockState(AIR)),
    ),
) {
    fun encode(section: ChunkSection): ByteArray {
        val layers = listOf(section.blocks) + section.extraBlockLayers
        val output = ByteArrayOutputStream()
        output.write(VERSION_WITHOUT_PAYLOAD_SECTION_Y)
        output.write(layers.size)
        layers.forEach { layer -> writeBlockStorage(output, layer) }
        return output.toByteArray()
    }

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
        val resolvedSectionY = resolveSectionY(version, sectionY, reader) ?: return null

        val blockLayers = List(storageCount) {
            readBlockStorage(reader) ?: return null
        }
        return ChunkSection(
            y = resolvedSectionY,
            blocks = blockLayers.first(),
            biomes = emptyBiomePalette,
            light = LightData(blockLight = ByteArray(2048), skyLight = ByteArray(2048)),
            extraBlockLayers = blockLayers.drop(1),
        )
    }

    private fun resolveSectionY(version: Int, keySectionY: Int, reader: PayloadReader): Int? {
        if (version != VERSION_WITH_PAYLOAD_SECTION_Y) {
            return keySectionY
        }

        val payloadSectionY = reader.readByteOrNull()?.toInt() ?: return null
        return if (payloadSectionY == keySectionY) payloadSectionY else null
    }

    private fun writeBlockStorage(output: ByteArrayOutputStream, storage: PalettedContainer<BlockState>) {
        val bitsPerBlock = storage.bitsPerEntry
        require(bitsPerBlock <= Int.SIZE_BITS) { "Bedrock block storage cannot encode $bitsPerBlock bits per block." }
        output.write((bitsPerBlock shl 1) or RUNTIME_PALETTE_FLAG)
        if (bitsPerBlock > 0) {
            packToBedrockWords(storage).forEach { word -> output.writeLittleEndianInt(word) }
        }
        output.writeUnsignedVarInt(storage.palette.size)
        storage.palette.forEach { state ->
            output.writeUnsignedVarInt(runtimeIdTranslator.toBedrockRuntimeId(state) ?: AIR_RUNTIME_ID)
        }
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
            bitsPerEntry = bitsPerBlock,
            wordBits = Int.SIZE_BITS,
        )
    }

    private fun packToBedrockWords(storage: PalettedContainer<BlockState>): IntArray {
        val bitsPerBlock = storage.bitsPerEntry
        val valuesPerWord = Int.SIZE_BITS / bitsPerBlock
        require(valuesPerWord > 0) { "bitsPerBlock must allow at least one value per Bedrock word." }
        val wordCount = (BLOCKS_PER_SECTION + valuesPerWord - 1) / valuesPerWord
        val mask = if (bitsPerBlock == Int.SIZE_BITS) {
            0xffff_ffffL
        } else {
            (1L shl bitsPerBlock) - 1L
        }
        val words = IntArray(wordCount)
        repeat(BLOCKS_PER_SECTION) { index ->
            val paletteIndex = storage.paletteIndexLongAt(index)
            require(paletteIndex in 0..mask) {
                "Palette index $paletteIndex cannot be encoded with $bitsPerBlock bits."
            }
            val wordIndex = index / valuesPerWord
            val bitOffset = (index % valuesPerWord) * bitsPerBlock
            words[wordIndex] = words[wordIndex] or ((paletteIndex and mask).toInt() shl bitOffset)
        }
        return words
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

        fun readByteOrNull(): Byte? {
            if (offset >= payload.size) {
                return null
            }
            return payload[offset++]
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

    private fun ByteArrayOutputStream.writeLittleEndianInt(value: Int) {
        write(value and 0xff)
        write((value ushr 8) and 0xff)
        write((value ushr 16) and 0xff)
        write((value ushr 24) and 0xff)
    }

    private fun ByteArrayOutputStream.writeUnsignedVarInt(value: Int) {
        var remaining = value
        do {
            var byte = remaining and 0x7f
            remaining = remaining ushr 7
            if (remaining != 0) {
                byte = byte or 0x80
            }
            write(byte)
        } while (remaining != 0)
    }

    private companion object {
        const val VERSION_WITHOUT_PAYLOAD_SECTION_Y = 8
        const val BLOCKS_PER_SECTION = 16 * 16 * 16
        const val MAX_VAR_INT_BYTES = 5
        const val VERSION_WITH_PAYLOAD_SECTION_Y = 9
        const val RUNTIME_PALETTE_FLAG = 1
        const val AIR_RUNTIME_ID = 0
        val SUPPORTED_VERSIONS = setOf(8, 9)
        val AIR = RegistryKey.parse("minecraft:air")
        val UNKNOWN_BEDROCK_BLOCK = RegistryKey.molten("unknown_bedrock_block")
        val emptyBiomePalette = PalettedContainer(
            palette = listOf(RegistryKey.parse("minecraft:plains")),
            packedData = longArrayOf(0),
        )
    }
}
