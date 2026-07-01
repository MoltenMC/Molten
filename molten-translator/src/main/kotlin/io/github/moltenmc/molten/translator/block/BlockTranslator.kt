package io.github.moltenmc.molten.translator.block

import io.github.moltenmc.molten.common.world.BlockState

interface BlockTranslator {
    fun toExternal(state: BlockState): BlockState
}
