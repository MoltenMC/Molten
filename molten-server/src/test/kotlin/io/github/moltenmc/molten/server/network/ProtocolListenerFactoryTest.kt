package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition
import io.github.moltenmc.molten.server.runtime.RuntimeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProtocolListenerFactoryTest {
    @Test
    fun createsOnlyJavaListenerForJavaOnlyRuntime() {
        val listeners = factory().create(RuntimeDefinition.forMode(RuntimeMode.JAVA_ONLY))

        assertEquals(listOf(ProtocolStack.JAVA_EDITION), listeners.map { it.protocol })
        assertIs<JavaProtocolListener>(listeners.single())
    }

    @Test
    fun createsOnlyBedrockListenerForBedrockOnlyRuntime() {
        val listeners = factory().create(RuntimeDefinition.forMode(RuntimeMode.BEDROCK_ONLY))

        assertEquals(listOf(ProtocolStack.BEDROCK_EDITION), listeners.map { it.protocol })
        assertIs<BedrockProtocolListener>(listeners.single())
    }

    @Test
    fun createsBothListenersForHybridRuntimes() {
        val javaBased = factory().create(RuntimeDefinition.forMode(RuntimeMode.JAVA_BASED))
        val bedrockBased = factory().create(RuntimeDefinition.forMode(RuntimeMode.BEDROCK_BASED))

        assertEquals(
            listOf(ProtocolStack.JAVA_EDITION, ProtocolStack.BEDROCK_EDITION),
            javaBased.map { it.protocol },
        )
        assertEquals(
            listOf(ProtocolStack.JAVA_EDITION, ProtocolStack.BEDROCK_EDITION),
            bedrockBased.map { it.protocol },
        )
    }

    private fun factory(): ProtocolListenerFactory =
        ProtocolListenerFactory(ServerConfiguration.defaults())
}
