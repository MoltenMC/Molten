package io.github.moltenmc.molten.server.runtime

import io.github.moltenmc.molten.common.world.WorldStorageKind

data class RuntimeDefinition(
    val mode: RuntimeMode,
    val enabledProtocols: Set<ProtocolStack>,
    val primaryStorage: WorldStorageKind,
    val secondaryStorage: WorldStorageKind? = null,
    val translationPolicy: TranslationPolicy,
) {
    companion object {
        fun forMode(mode: RuntimeMode): RuntimeDefinition = when (mode) {
            RuntimeMode.JAVA_BASED -> RuntimeDefinition(
                mode = mode,
                enabledProtocols = setOf(ProtocolStack.JAVA_EDITION, ProtocolStack.BEDROCK_EDITION),
                primaryStorage = WorldStorageKind.JAVA_ANVIL,
                secondaryStorage = WorldStorageKind.BEDROCK_LEVELDB,
                translationPolicy = TranslationPolicy.JAVA_SEMANTICS,
            )
            RuntimeMode.JAVA_ONLY -> RuntimeDefinition(
                mode = mode,
                enabledProtocols = setOf(ProtocolStack.JAVA_EDITION),
                primaryStorage = WorldStorageKind.JAVA_ANVIL,
                translationPolicy = TranslationPolicy.REGISTRY_NORMALIZATION_ONLY,
            )
            RuntimeMode.BEDROCK_BASED -> RuntimeDefinition(
                mode = mode,
                enabledProtocols = setOf(ProtocolStack.JAVA_EDITION, ProtocolStack.BEDROCK_EDITION),
                primaryStorage = WorldStorageKind.BEDROCK_LEVELDB,
                secondaryStorage = WorldStorageKind.JAVA_ANVIL,
                translationPolicy = TranslationPolicy.BEDROCK_SEMANTICS,
            )
            RuntimeMode.BEDROCK_ONLY -> RuntimeDefinition(
                mode = mode,
                enabledProtocols = setOf(ProtocolStack.BEDROCK_EDITION),
                primaryStorage = WorldStorageKind.BEDROCK_LEVELDB,
                translationPolicy = TranslationPolicy.REGISTRY_NORMALIZATION_ONLY,
            )
        }
    }
}
