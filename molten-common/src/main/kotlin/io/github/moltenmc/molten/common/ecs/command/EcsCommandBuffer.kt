package io.github.moltenmc.molten.common.ecs.command

import io.github.moltenmc.molten.common.ecs.StructuralChange
import io.github.moltenmc.molten.common.ecs.StructuralChangeBuffer

class EcsCommandBuffer(
    private val delegate: StructuralChangeBuffer = StructuralChangeBuffer(),
) {
    fun enqueue(command: StructuralChange) {
        delegate.enqueue(command)
    }

    fun flush(): List<StructuralChange> = delegate.drain()
}
