package io.github.moltenmc.molten.common.ecs.sparse

import io.github.moltenmc.molten.common.ecs.Component
import io.github.moltenmc.molten.common.ecs.EntityId

class SparseSet<T : Component> {
    private val denseEntities = ArrayList<EntityId>()
    private val denseComponents = ArrayList<T>()
    private val sparse = HashMap<Int, Int>()

    fun contains(entityId: EntityId): Boolean = sparse.containsKey(entityId.index)

    fun get(entityId: EntityId): T? = sparse[entityId.index]?.let(denseComponents::get)

    fun put(entityId: EntityId, component: T) {
        val existingIndex = sparse[entityId.index]
        if (existingIndex != null) {
            denseComponents[existingIndex] = component
            return
        }

        sparse[entityId.index] = denseEntities.size
        denseEntities += entityId
        denseComponents += component
    }

    fun remove(entityId: EntityId): T? {
        val denseIndex = sparse.remove(entityId.index) ?: return null
        val removed = denseComponents[denseIndex]
        val lastIndex = denseEntities.lastIndex

        if (denseIndex != lastIndex) {
            val movedEntity = denseEntities[lastIndex]
            denseEntities[denseIndex] = movedEntity
            denseComponents[denseIndex] = denseComponents[lastIndex]
            sparse[movedEntity.index] = denseIndex
        }

        denseEntities.removeAt(lastIndex)
        denseComponents.removeAt(lastIndex)
        return removed
    }
}
