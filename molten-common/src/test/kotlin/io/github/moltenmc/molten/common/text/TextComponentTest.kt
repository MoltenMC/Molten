package io.github.moltenmc.molten.common.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextComponentTest {
    @Test
    fun storesPlainText() {
        assertEquals("Hello", TextComponent("Hello").plainText)
    }

    @Test
    fun rejectsEmptyText() {
        assertFailsWith<IllegalArgumentException> {
            TextComponent("")
        }
    }
}
