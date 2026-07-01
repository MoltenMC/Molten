package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.nbt.NbtValue
import io.github.moltenmc.molten.common.registry.RegistryKey
import io.github.moltenmc.molten.common.world.BlockPos

data class BlockEntityData(
    val position: BlockPos,
    val type: RegistryKey,
    val rawNbt: NbtValue.CompoundValue,
)
