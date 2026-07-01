package io.github.moltenmc.molten.translator.entity

import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.math.Vec3d
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.translator.capability.CapabilityFlag

data class EntityTranslationMapping(
    val internalEntityType: EntityKind,
    val javaEntityType: RegistryKey?,
    val bedrockEntityIdentifier: RegistryKey?,
    val boundingBox: Vec3d,
    val defaultAttributes: Map<RegistryKey, Double>,
    val metadataSchema: Set<MetadataCategory>,
    val spawnRules: Set<String>,
    val capabilities: Set<CapabilityFlag>,
)

enum class MetadataCategory {
    FLAGS,
    POSE,
    HEALTH,
    AIR,
    CUSTOM_NAME,
    EFFECTS,
    EQUIPMENT,
    VARIANT,
    OWNER,
    PASSENGERS,
}
