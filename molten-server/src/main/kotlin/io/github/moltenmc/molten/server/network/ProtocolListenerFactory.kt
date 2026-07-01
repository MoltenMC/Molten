package io.github.moltenmc.molten.server.network

import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import io.github.moltenmc.molten.server.runtime.RuntimeDefinition

class ProtocolListenerFactory(
    private val configuration: ServerConfiguration,
) {
    fun create(runtimeDefinition: RuntimeDefinition): List<ProtocolListener> =
        ProtocolStack.entries
            .filter { protocol -> protocol in runtimeDefinition.enabledProtocols }
            .map { protocol -> create(protocol) }

    private fun create(protocol: ProtocolStack): ProtocolListener =
        when (protocol) {
            ProtocolStack.JAVA_EDITION -> JavaProtocolListener(configuration)
            ProtocolStack.BEDROCK_EDITION -> BedrockProtocolListener(configuration)
        }
}
