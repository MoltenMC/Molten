package io.github.moltenmc.molten.common.nbt

import kotlin.test.Test
import kotlin.test.assertEquals

class NbtCodecTest {
    @Test
    fun roundTripsJavaNbt() {
        val original = sampleCompound()

        val bytes = NbtCodec.encode(original, NbtFormat.JAVA, "root")
        val decoded = NbtCodec.decode(bytes, NbtFormat.JAVA)

        assertEquals(original, decoded)
    }

    @Test
    fun roundTripsBedrockNbt() {
        val original = sampleCompound()

        val bytes = NbtCodec.encode(original, NbtFormat.BEDROCK, "root")
        val decoded = NbtCodec.decode(bytes, NbtFormat.BEDROCK)

        assertEquals(original, decoded)
    }

    private fun sampleCompound(): NbtValue.CompoundValue =
        NbtValue.CompoundValue(
            mapOf(
                "name" to NbtValue.StringValue("Molten"),
                "javaProtocol" to NbtValue.IntValue(776),
                "bedrockProtocol" to NbtValue.IntValue(1001),
                "nested" to NbtValue.CompoundValue(
                    mapOf("enabled" to NbtValue.ByteValue(1)),
                ),
            ),
        )
}
