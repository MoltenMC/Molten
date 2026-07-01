package io.github.moltenmc.molten.common.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.world.BlockPos

sealed interface ServerIntent {
    val sourceEntityId: EntityId?
    val routing: IntentRouting

    data class PlayerMove(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float,
    ) : ServerIntent

    data class PlayerChat(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val message: String,
    ) : ServerIntent

    data class PlayerCommand(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val commandLine: String,
    ) : ServerIntent

    data class BlockInteract(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val position: BlockPos,
    ) : ServerIntent

    data class BlockBreak(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val position: BlockPos,
    ) : ServerIntent

    data class InventoryAction(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val actionId: Int,
    ) : ServerIntent

    data class EntityInteract(
        override val sourceEntityId: EntityId,
        override val routing: IntentRouting,
        val targetEntityId: EntityId,
    ) : ServerIntent

    data class ClientSettings(
        override val sourceEntityId: EntityId?,
        override val routing: IntentRouting,
        val locale: String,
        val viewDistance: Int,
    ) : ServerIntent

    data class PluginMessage(
        override val sourceEntityId: EntityId?,
        override val routing: IntentRouting,
        val channel: String,
        val payload: ByteArray,
    ) : ServerIntent {
        override fun equals(other: Any?): Boolean =
            other is PluginMessage &&
                sourceEntityId == other.sourceEntityId &&
                routing == other.routing &&
                channel == other.channel &&
                payload.contentEquals(other.payload)

        override fun hashCode(): Int =
            31 * (31 * (31 * sourceEntityId.hashCode() + routing.hashCode()) + channel.hashCode()) +
                payload.contentHashCode()
    }

    data class Disconnect(
        override val sourceEntityId: EntityId?,
        override val routing: IntentRouting,
        val reason: String,
    ) : ServerIntent
}
