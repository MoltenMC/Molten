package io.github.moltenmc.molten.bedrock.world.leveldb

object BedrockFinalizedStateDecoder {
    const val RAW_DATA_KEY = "bedrock:finalized_state"

    fun decode(payload: ByteArray): Int? =
        when {
            payload.size >= Int.SIZE_BYTES -> littleEndianInt(payload)
            payload.size == Byte.SIZE_BYTES -> payload[0].toInt() and 0xff
            else -> null
        }

    fun encode(value: Int): ByteArray =
        byteArrayOf(
            (value and 0xff).toByte(),
            ((value ushr 8) and 0xff).toByte(),
            ((value ushr 16) and 0xff).toByte(),
            ((value ushr 24) and 0xff).toByte(),
        )

    private fun littleEndianInt(payload: ByteArray): Int =
        (payload[0].toInt() and 0xff) or
            ((payload[1].toInt() and 0xff) shl 8) or
            ((payload[2].toInt() and 0xff) shl 16) or
            ((payload[3].toInt() and 0xff) shl 24)
}
