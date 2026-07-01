package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.Buffer
import io.netty5.channel.ChannelHandlerContext
import io.netty5.handler.codec.ByteToMessageDecoder
import io.netty5.handler.codec.DecoderException
import io.netty5.handler.codec.TooLongFrameException

class JavaVarIntFrameDecoder(
    private val maxFrameBytes: Int = JavaPacketFrame.DEFAULT_MAX_FRAME_BYTES,
) : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, input: Buffer) {
        try {
            while (input.readableBytes() > 0) {
                val payload = JavaPacketFrame.tryReadPayload(input, maxFrameBytes) ?: return
                ctx.fireChannelRead(payload)
            }
        } catch (error: IllegalArgumentException) {
            if (error.message?.contains("exceeds limit") == true) {
                throw TooLongFrameException(error.message, error)
            }
            throw DecoderException(error.message, error)
        }
    }
}
