package io.github.moltenmc.molten.common.region

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.world.BlockPos
import io.github.moltenmc.molten.common.world.ChunkPos

sealed interface RegionMessage {
    data class EntityMigration(val entityId: EntityId, val target: RegionPos) : RegionMessage

    data class NeighborBlockUpdate(val position: BlockPos) : RegionMessage

    data class ExplosionPropagation(val origin: BlockPos, val radius: Float) : RegionMessage

    data class ProjectileCrossRegion(val entityId: EntityId, val target: RegionPos) : RegionMessage

    data class ChatVisibility(val entityId: EntityId, val target: RegionPos) : RegionMessage

    data class ChunkSubscription(val entityId: EntityId, val chunkPos: ChunkPos) : RegionMessage
}
