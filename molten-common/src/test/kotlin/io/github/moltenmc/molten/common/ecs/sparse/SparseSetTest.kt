package io.github.moltenmc.molten.common.ecs.sparse

import io.github.moltenmc.molten.common.ecs.Component
import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.ecs.EntityKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SparseSetTest {
    @Test
    fun supportsPutGetAndRemove() {
        val set = SparseSet<TestComponent>()
        val entityId = EntityId.of(index = 1, generation = 0, kind = EntityKind.GENERIC)

        set.put(entityId, TestComponent("value"))

        assertTrue(set.contains(entityId))
        assertEquals(TestComponent("value"), set.get(entityId))
        assertEquals(TestComponent("value"), set.remove(entityId))
        assertFalse(set.contains(entityId))
    }

    private data class TestComponent(val value: String) : Component
}
