package io.github.moltenmc.molten.java.network.codec

import io.netty5.buffer.Buffer
import io.netty5.channel.ChannelHandlerContext
import io.netty5.handler.codec.MessageToByteEncoder

class JavaVarIntFrameEncoder : MessageToByteEncoder<Buffer>(Buffer::class.java) {
    override fun allocateBuffer(ctx: ChannelHandlerContext, msg: Buffer): Buffer =
        ctx.bufferAllocator().allocate(JavaVarInt.encodedSize(msg.readableBytes()) + msg.readableBytes())

    override fun encode(ctx: ChannelHandlerContext, msg: Buffer, out: Buffer) {
        JavaPacketFrame.writePayload(msg, out)
    }
}
