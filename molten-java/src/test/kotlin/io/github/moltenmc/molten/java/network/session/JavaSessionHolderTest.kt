package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.network.message.OutboundMessage
import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.text.TextComponent
import io.github.moltenmc.molten.common.world.WorldId
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaSessionHolderTest {
    @Test
    fun ownsOutboundQueue() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)

        sessionHolder.outboundQueue.enqueue(OutboundMessage.System(TextComponent("Hello")))

        assertEquals(1, sessionHolder.outboundQueue.size)
    }

    @Test
    fun storesIntentRoutingContext() {
        val sessionHolder = JavaSessionHolder(JavaProtocolState.PLAY)
        val entityId = EntityId.of(1, generation = 0, EntityKind.PLAYER)
        val worldId = WorldId(UUID.fromString("12345678-1234-5678-9abc-def012345678"))
        val regionPos = RegionPos(2, 3)

        sessionHolder.playerEntityId = entityId
        sessionHolder.currentWorld = worldId
        sessionHolder.currentRegion = regionPos

        assertEquals(entityId, sessionHolder.playerEntityId)
        assertEquals(worldId, sessionHolder.currentWorld)
        assertEquals(regionPos, sessionHolder.currentRegion)
    }
}
