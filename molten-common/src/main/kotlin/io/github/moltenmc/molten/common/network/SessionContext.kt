package io.github.moltenmc.molten.common.network

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.net.SocketAddress
import java.util.UUID

data class SessionContext(
    val connectionId: UUID,
    val remoteAddress: SocketAddress?,
    val protocolVersion: Int,
    val playerEntityId: EntityId? = null,
    val currentWorld: WorldId? = null,
    val currentRegion: RegionPos? = null,
)
