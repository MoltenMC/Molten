package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.Buffer

object JavaPacketFrame {
    const val DEFAULT_MAX_FRAME_BYTES: Int = 2 * 1024 * 1024

    fun tryReadPayload(input: Buffer, maxFrameBytes: Int = DEFAULT_MAX_FRAME_BYTES): Buffer? {
        require(maxFrameBytes > 0) { "Maximum frame size must be positive." }

        val frameStart = input.readerOffset()
        val payloadLength = JavaVarInt.readOrNull(input) ?: return null
        if (payloadLength < 0 || payloadLength > maxFrameBytes) {
            input.readerOffset(frameStart)
            throw IllegalArgumentException("Java packet frame length $payloadLength exceeds limit $maxFrameBytes.")
        }
        if (input.readableBytes() < payloadLength) {
            input.readerOffset(frameStart)
            return null
        }
        return input.readSplit(payloadLength)
    }

    fun writePayload(payload: Buffer, output: Buffer) {
        JavaVarInt.write(payload.readableBytes(), output)
        output.writeBytes(payload)
    }
}
