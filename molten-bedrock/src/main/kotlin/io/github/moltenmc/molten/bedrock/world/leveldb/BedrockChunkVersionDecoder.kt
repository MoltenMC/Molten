package io.github.moltenmc.molten.bedrock.world.leveldb

object BedrockChunkVersionDecoder {
    const val RAW_DATA_KEY = "bedrock:chunk_version"

    fun decode(payload: ByteArray): Int? =
        when {
            payload.isEmpty() -> null
            payload.size >= Int.SIZE_BYTES -> (payload[0].toInt() and 0xff) or
                ((payload[1].toInt() and 0xff) shl 8) or
                ((payload[2].toInt() and 0xff) shl 16) or
                ((payload[3].toInt() and 0xff) shl 24)
            else -> payload[0].toInt() and 0xff
        }

    fun encode(version: Int): ByteArray {
        require(version in 0..0xff) { "Bedrock chunk version $version is outside byte range." }
        return byteArrayOf((version and 0xff).toByte())
    }
}
