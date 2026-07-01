package io.github.moltenmc.molten.translator.nbt

import io.github.moltenmc.molten.common.nbt.NbtValue

interface NbtTranslator {
    fun translate(value: NbtValue): NbtValue
}
