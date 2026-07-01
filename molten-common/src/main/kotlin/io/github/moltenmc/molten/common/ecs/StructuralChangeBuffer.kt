package io.github.moltenmc.molten.common.ecs

class StructuralChangeBuffer {
    private val pendingChanges = ArrayList<StructuralChange>()

    fun enqueue(change: StructuralChange) {
        pendingChanges += change
    }

    fun drain(): List<StructuralChange> {
        val drained = pendingChanges.toList()
        pendingChanges.clear()
        return drained
    }
}
