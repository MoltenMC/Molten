package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.Buffer
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

object JavaStringCodec {
    fun read(input: Buffer, maxCharacters: Int): String {
        require(maxCharacters >= 0) { "Maximum character count must not be negative." }

        val length = JavaVarInt.readOrNull(input)
            ?: throw IllegalArgumentException("Incomplete string length.")
        val maxBytes = maxCharacters * MAX_UTF8_BYTES_PER_CHARACTER
        require(length in 0..maxBytes) { "String byte length $length exceeds limit $maxBytes." }
        require(input.readableBytes() >= length) { "Incomplete string payload." }

        val bytes = ByteArray(length)
        input.readBytes(bytes, 0, length)
        val value = decodeUtf8(bytes)
        require(value.length <= maxCharacters) {
            "String character length ${value.length} exceeds limit $maxCharacters."
        }
        return value
    }

    fun write(value: String, output: Buffer, maxCharacters: Int) {
        require(maxCharacters >= 0) { "Maximum character count must not be negative." }
        require(value.length <= maxCharacters) {
            "String character length ${value.length} exceeds limit $maxCharacters."
        }

        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        val maxBytes = maxCharacters * MAX_UTF8_BYTES_PER_CHARACTER
        require(bytes.size <= maxBytes) { "String byte length ${bytes.size} exceeds limit $maxBytes." }

        JavaVarInt.write(bytes.size, output)
        output.writeBytes(bytes)
    }

    private fun decodeUtf8(bytes: ByteArray): String {
        val decoder = StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return decoder.decode(ByteBuffer.wrap(bytes)).toString()
    }

    private const val MAX_UTF8_BYTES_PER_CHARACTER = 4
}
