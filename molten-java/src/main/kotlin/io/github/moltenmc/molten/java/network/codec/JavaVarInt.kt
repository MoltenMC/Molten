package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.Buffer

object JavaVarInt {
    const val MAX_BYTES: Int = 5

    fun encodedSize(value: Int): Int {
        var remaining = value
        var size = 0
        do {
            remaining = remaining ushr 7
            size++
        } while (remaining != 0)
        return size
    }

    fun write(value: Int, output: Buffer) {
        var remaining = value
        do {
            var next = remaining and SEGMENT_BITS
            remaining = remaining ushr 7
            if (remaining != 0) {
                next = next or CONTINUE_BIT
            }
            output.writeByte(next.toByte())
        } while (remaining != 0)
    }

    fun readOrNull(input: Buffer): Int? {
        val startOffset = input.readerOffset()
        var value = 0
        var position = 0

        while (position < MAX_BYTES) {
            if (input.readableBytes() == 0) {
                input.readerOffset(startOffset)
                return null
            }

            val current = input.readUnsignedByte()
            value = value or ((current and SEGMENT_BITS) shl (position * 7))
            if ((current and CONTINUE_BIT) == 0) {
                return value
            }
            position++
        }

        input.readerOffset(startOffset)
        throw IllegalArgumentException("VarInt exceeds $MAX_BYTES bytes.")
    }

    fun encode(value: Int): ByteArray {
        val bytes = ByteArray(encodedSize(value))
        var remaining = value
        var index = 0
        do {
            var next = remaining and SEGMENT_BITS
            remaining = remaining ushr 7
            if (remaining != 0) {
                next = next or CONTINUE_BIT
            }
            bytes[index++] = next.toByte()
        } while (remaining != 0)
        return bytes
    }

    private const val SEGMENT_BITS = 0x7F
    private const val CONTINUE_BIT = 0x80
}
