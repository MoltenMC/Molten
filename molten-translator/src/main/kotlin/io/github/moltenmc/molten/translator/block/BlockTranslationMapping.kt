package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockState
import io.github.moltenmc.molten.translator.capability.CapabilityFlag

data class BlockTranslationMapping(
    val namespaceKey: RegistryKey,
    val stateProperties: Map<String, String>,
    val collisionShape: String,
    val visualShape: String,
    val lightEmission: Int,
    val lightOpacity: Int,
    val hardness: Float,
    val toolRequirements: Set<RegistryKey>,
    val waterloggable: Boolean,
    val platformCapabilities: Set<CapabilityFlag>,
    val internalState: BlockState,
    val javaBlockStateId: Int?,
    val bedrockRuntimeId: Int?,
)
