package io.github.moltenmc.molten.common.ecs

import kotlin.test.Test
import kotlin.test.assertEquals

class EntityIdTest {
    @Test
    fun packsAndUnpacksEntityIdFields() {
        val entityId = EntityId.of(index = 42, generation = 7, kind = EntityKind.PLAYER)

        assertEquals(42, entityId.index)
        assertEquals(7, entityId.generation)
        assertEquals(EntityKind.PLAYER, entityId.kind)
    }
}
