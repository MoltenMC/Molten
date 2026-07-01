package io.github.moltenmc.molten.common.nbt

import io.github.moltenmc.nbt.NbtByte
import io.github.moltenmc.nbt.NbtByteArray
import io.github.moltenmc.nbt.NbtCompound
import io.github.moltenmc.nbt.NbtDouble
import io.github.moltenmc.nbt.NbtEnd
import io.github.moltenmc.nbt.NbtFloat
import io.github.moltenmc.nbt.NbtInt
import io.github.moltenmc.nbt.NbtIntArray
import io.github.moltenmc.nbt.NbtList
import io.github.moltenmc.nbt.NbtLong
import io.github.moltenmc.nbt.NbtLongArray
import io.github.moltenmc.nbt.NbtShort
import io.github.moltenmc.nbt.NbtString
import io.github.moltenmc.nbt.NbtTag

object MoltenNbtAdapter {
    fun fromLibrary(tag: NbtTag): NbtValue = when (tag) {
        is NbtEnd -> NbtValue.EndValue
        is NbtByte -> NbtValue.ByteValue(tag.value)
        is NbtShort -> NbtValue.ShortValue(tag.value)
        is NbtInt -> NbtValue.IntValue(tag.value)
        is NbtLong -> NbtValue.LongValue(tag.value)
        is NbtFloat -> NbtValue.FloatValue(tag.value)
        is NbtDouble -> NbtValue.DoubleValue(tag.value)
        is NbtByteArray -> NbtValue.ByteArrayValue(tag.value)
        is NbtString -> NbtValue.StringValue(tag.value)
        is NbtList -> NbtValue.ListValue(tag.tags.map(::fromLibrary))
        is NbtCompound -> NbtValue.CompoundValue(tag.tags.mapValues { fromLibrary(it.value) })
        is NbtIntArray -> NbtValue.IntArrayValue(tag.value)
        is NbtLongArray -> NbtValue.LongArrayValue(tag.value)
    }

    fun toLibrary(value: NbtValue): NbtTag = when (value) {
        NbtValue.EndValue -> NbtEnd
        is NbtValue.ByteValue -> NbtByte(value.value)
        is NbtValue.ShortValue -> NbtShort(value.value)
        is NbtValue.IntValue -> NbtInt(value.value)
        is NbtValue.LongValue -> NbtLong(value.value)
        is NbtValue.FloatValue -> NbtFloat(value.value)
        is NbtValue.DoubleValue -> NbtDouble(value.value)
        is NbtValue.ByteArrayValue -> NbtByteArray(value.value)
        is NbtValue.StringValue -> NbtString(value.value)
        is NbtValue.ListValue -> value.toLibraryList()
        is NbtValue.CompoundValue -> value.buildLibraryCompound()
        is NbtValue.IntArrayValue -> NbtIntArray(value.value)
        is NbtValue.LongArrayValue -> NbtLongArray(value.value)
    }

    fun fromLibraryCompound(compound: NbtCompound): NbtValue.CompoundValue =
        fromLibrary(compound) as NbtValue.CompoundValue

    fun toLibraryCompound(value: NbtValue.CompoundValue): NbtCompound = value.buildLibraryCompound()

    private fun NbtValue.ListValue.toLibraryList(): NbtList {
        val tags = values.map(::toLibrary)
        val elementType = tags.firstOrNull()?.tagId() ?: NbtTagType.END
        val list = NbtList(elementType)
        tags.forEach(list::add)
        return list
    }

    private fun NbtValue.CompoundValue.buildLibraryCompound(): NbtCompound {
        val compound = NbtCompound()
        values.forEach { (key, value) -> compound.set(key, toLibrary(value)) }
        return compound
    }

    private fun NbtTag.tagId(): Byte = when (this) {
        is NbtEnd -> NbtTagType.END
        is NbtByte -> NbtTagType.BYTE
        is NbtShort -> NbtTagType.SHORT
        is NbtInt -> NbtTagType.INT
        is NbtLong -> NbtTagType.LONG
        is NbtFloat -> NbtTagType.FLOAT
        is NbtDouble -> NbtTagType.DOUBLE
        is NbtByteArray -> NbtTagType.BYTE_ARRAY
        is NbtString -> NbtTagType.STRING
        is NbtList -> NbtTagType.LIST
        is NbtCompound -> NbtTagType.COMPOUND
        is NbtIntArray -> NbtTagType.INT_ARRAY
        is NbtLongArray -> NbtTagType.LONG_ARRAY
    }

    private object NbtTagType {
        const val END: Byte = 0
        const val BYTE: Byte = 1
        const val SHORT: Byte = 2
        const val INT: Byte = 3
        const val LONG: Byte = 4
        const val FLOAT: Byte = 5
        const val DOUBLE: Byte = 6
        const val BYTE_ARRAY: Byte = 7
        const val STRING: Byte = 8
        const val LIST: Byte = 9
        const val COMPOUND: Byte = 10
        const val INT_ARRAY: Byte = 11
        const val LONG_ARRAY: Byte = 12
    }
}
