package io.github.moltenmc.molten.api.entity

import io.github.moltenmc.molten.api.item.ItemStack

interface ItemEntity : Entity {
    val itemStack: ItemStack
}
