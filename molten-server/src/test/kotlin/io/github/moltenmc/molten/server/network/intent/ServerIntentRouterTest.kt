package io.github.moltenmc.molten.server.network.intent

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerIntentRouterTest {
    @Test
    fun routesCompleteRegionIntentsToRegionSink() {
        val regionRoutes = mutableListOf<RegionRoute>()
        val globalIntents = mutableListOf<ServerIntent>()
        val router = ServerIntentRouter(
            regionSink = { worldId, regionPos, intent -> regionRoutes += RegionRoute(worldId, regionPos, intent) },
            globalSink = globalIntents::add,
        )
        val worldId = WorldId(UUID(0, 1))
        val regionPos = RegionPos(2, 3)
        val intent = chatIntent("region", IntentRouting(worldId, regionPos))

        router.accept(intent)

        assertEquals(listOf(RegionRoute(worldId, regionPos, intent)), regionRoutes)
        assertEquals(emptyList(), globalIntents)
    }

    @Test
    fun routesIncompleteRegionIntentsToGlobalSink() {
        val regionRoutes = mutableListOf<RegionRoute>()
        val globalIntents = mutableListOf<ServerIntent>()
        val router = ServerIntentRouter(
            regionSink = { worldId, regionPos, intent -> regionRoutes += RegionRoute(worldId, regionPos, intent) },
            globalSink = globalIntents::add,
        )
        val intent = chatIntent("global", IntentRouting(worldId = null, regionPos = RegionPos(2, 3)))

        router.accept(intent)

        assertEquals(emptyList(), regionRoutes)
        val expected: List<ServerIntent> = listOf(intent)
        assertEquals(expected, globalIntents)
    }

    private fun chatIntent(
        message: String,
        routing: IntentRouting,
    ): ServerIntent.PlayerChat =
        ServerIntent.PlayerChat(
            sourceEntityId = EntityId.of(1, generation = 0, EntityKind.PLAYER),
            routing = routing,
            message = message,
        )

    private data class RegionRoute(
        val worldId: WorldId,
        val regionPos: RegionPos,
        val intent: ServerIntent,
    )
}
