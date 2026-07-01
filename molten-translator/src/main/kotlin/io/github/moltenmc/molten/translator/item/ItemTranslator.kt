package io.github.moltenmc.molten.translator.item

interface ItemTranslator {
    fun toExternal(stack: InternalItemStack): InternalItemStack
}
