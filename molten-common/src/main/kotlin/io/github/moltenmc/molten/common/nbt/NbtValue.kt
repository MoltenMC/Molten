package io.github.moltenmc.molten.common.nbt

sealed interface NbtValue {
    data object EndValue : NbtValue

    data class ByteValue(val value: Byte) : NbtValue

    data class ShortValue(val value: Short) : NbtValue

    data class StringValue(val value: String) : NbtValue

    data class IntValue(val value: Int) : NbtValue

    data class LongValue(val value: Long) : NbtValue

    data class FloatValue(val value: Float) : NbtValue

    data class DoubleValue(val value: Double) : NbtValue

    data class ByteArrayValue(val value: ByteArray) : NbtValue {
        override fun equals(other: Any?): Boolean =
            other is ByteArrayValue && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }

    data class ListValue(val values: List<NbtValue>) : NbtValue

    data class CompoundValue(val values: Map<String, NbtValue>) : NbtValue

    data class IntArrayValue(val value: IntArray) : NbtValue {
        override fun equals(other: Any?): Boolean =
            other is IntArrayValue && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }

    data class LongArrayValue(val value: LongArray) : NbtValue {
        override fun equals(other: Any?): Boolean =
            other is LongArrayValue && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }
}
