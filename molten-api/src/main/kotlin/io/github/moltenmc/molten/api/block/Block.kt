package io.github.moltenmc.molten.api.block

import io.github.moltenmc.molten.common.world.BlockPos
import io.github.moltenmc.molten.common.world.BlockState

interface Block {
    val position: BlockPos

    val state: BlockState
}
