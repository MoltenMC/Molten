package io.github.moltenmc.molten.java.network.codec

import io.github.moltenmc.molten.common.network.PacketDirection
import io.github.moltenmc.molten.common.network.ProtocolContext
import io.github.moltenmc.molten.java.JavaEditionProtocol
import io.github.moltenmc.molten.java.network.packet.HandshakeNextState
import io.github.moltenmc.molten.java.network.packet.HandshakePacket
import io.github.moltenmc.molten.java.network.packet.LoginStartPacket
import io.github.moltenmc.molten.java.network.registry.JavaPacketRegistries
import io.github.moltenmc.molten.java.network.session.JavaProtocolStateHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import io.netty5.buffer.BufferAllocator
import io.netty5.handler.codec.DecoderException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class JavaPacketDecoderTest {
    private val protocolContext = ProtocolContext(
        protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
        direction = PacketDirection.SERVERBOUND,
    )

    @Test
    fun decodesRegisteredHandshakePacket() {
        val handshake = HandshakePacket(
            packetId = JavaHandshakePacketCodec.PACKET_ID,
            protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            serverAddress = "localhost",
            serverPort = 25565,
            nextState = HandshakeNextState.LOGIN,
        )
        val payload = JavaHandshakePacketCodec().encode(handshake, protocolContext)

        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = allocator.copyOf(payload)
            val packet = JavaPacketDecoder().decodePayload(buffer)

            assertEquals(handshake, assertIs<HandshakePacket>(packet))

            buffer.close()
        }
    }

    @Test
    fun rejectsUnknownPacketId() {
        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = allocator.copyOf(byteArrayOf(0x7f))

            assertFailsWith<DecoderException> {
                JavaPacketDecoder().decodePayload(buffer)
            }

            buffer.close()
        }
    }

    @Test
    fun rejectsPacketRegisteredForDifferentState() {
        val handshake = HandshakePacket(
            packetId = JavaHandshakePacketCodec.PACKET_ID,
            protocolVersion = JavaEditionProtocol.PROTOCOL_VERSION,
            serverAddress = "localhost",
            serverPort = 25565,
            nextState = HandshakeNextState.STATUS,
        )
        val payload = JavaHandshakePacketCodec().encode(handshake, protocolContext)
        val decoder = JavaPacketDecoder(
            registry = JavaPacketRegistries.protocol776(),
            stateHolder = JavaProtocolStateHolder(JavaProtocolState.PLAY),
        )

        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = allocator.copyOf(payload)

            assertFailsWith<DecoderException> {
                decoder.decodePayload(buffer)
            }

            buffer.close()
        }
    }

    @Test
    fun rejectsDecodedPacketThatFailsValidation() {
        val loginStart = LoginStartPacket(
            packetId = JavaLoginStartPacketCodec.PACKET_ID,
            name = "bad-name",
            playerUuid = UUID.fromString("12345678-1234-5678-9abc-def012345678"),
        )
        val payload = JavaLoginStartPacketCodec().encode(loginStart, protocolContext)
        val decoder = JavaPacketDecoder(
            registry = JavaPacketRegistries.protocol776(),
            stateHolder = JavaProtocolStateHolder(JavaProtocolState.LOGIN),
        )

        BufferAllocator.onHeapUnpooled().use { allocator ->
            val buffer = allocator.copyOf(payload)

            val error = assertFailsWith<DecoderException> {
                decoder.decodePayload(buffer)
            }

            assertEquals("Login username contains invalid characters.", error.message)

            buffer.close()
        }
    }
}
