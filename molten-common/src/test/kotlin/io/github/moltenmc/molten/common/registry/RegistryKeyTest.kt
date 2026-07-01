package io.github.moltenmc.molten.common.registry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RegistryKeyTest {
    @Test
    fun parsesNamespacedKeys() {
        val key = RegistryKey.parse("minecraft:stone")

        assertEquals("minecraft", key.namespace)
        assertEquals("stone", key.value)
    }

    @Test
    fun rejectsKeysWithoutNamespace() {
        assertFailsWith<IllegalArgumentException> {
            RegistryKey.parse("stone")
        }
    }
}
