package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import io.github.moltenmc.molten.java.protocol.JavaProtocolState

class JavaSessionHolder(
    initialState: JavaProtocolState = JavaProtocolState.HANDSHAKE,
) : JavaProtocolStateHolder(initialState) {
    @Volatile
    var profile: JavaSessionProfile? = null

    @Volatile
    var playerEntityId: EntityId? = null

    @Volatile
    var currentWorld: WorldId? = null

    @Volatile
    var currentRegion: RegionPos? = null

    val outboundQueue: JavaOutboundQueue = JavaOutboundQueue()

    val inboundIntentQueue: JavaInboundIntentQueue = JavaInboundIntentQueue()
}
