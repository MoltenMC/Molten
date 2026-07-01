package io.github.moltenmc.molten.translator.item

import io.github.moltenmc.molten.common.nbt.NbtValue
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.translator.capability.CapabilityFlag

data class InternalItemStack(
    val itemType: RegistryKey,
    val amount: Int,
    val damage: Int = 0,
    val components: Map<String, NbtValue> = emptyMap(),
    val enchantments: Map<RegistryKey, Int> = emptyMap(),
    val customName: String? = null,
    val lore: List<String> = emptyList(),
    val nbt: NbtValue? = null,
    val capabilities: Set<CapabilityFlag> = emptySet(),
)
