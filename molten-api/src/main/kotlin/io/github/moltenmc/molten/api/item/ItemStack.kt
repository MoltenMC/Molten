package io.github.moltenmc.molten.api.item

import io.github.moltenmc.molten.common.registry.RegistryKey

data class ItemStack(
    val itemKey: RegistryKey,
    val amount: Int,
) {
    init {
        require(amount > 0) { "Item amount must be positive." }
    }
}
