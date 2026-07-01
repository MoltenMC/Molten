package io.github.moltenmc.molten.common.ecs

sealed interface StructuralChange {
    data class CreateEntity(
        val entityId: EntityId,
        val components: List<Component> = emptyList(),
    ) : StructuralChange

    data class RemoveEntity(val entityId: EntityId) : StructuralChange

    data class AddComponent(val entityId: EntityId, val component: Component) : StructuralChange

    data class RemoveComponent(val entityId: EntityId, val componentType: ComponentType) : StructuralChange

    data class SetComponent(val entityId: EntityId, val component: Component) : StructuralChange

    data class MoveEntityToRegion(
        val entityId: EntityId,
        val targetRegion: io.github.moltenmc.molten.common.region.RegionPos,
    ) : StructuralChange

    data class ScheduleEvent(val event: Any) : StructuralChange
}
