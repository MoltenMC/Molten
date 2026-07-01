package io.github.moltenmc.molten.java.status

import io.github.moltenmc.molten.common.text.TextComponent
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaChatComponentJsonTest {
    @Test
    fun encodesPlainTextComponent() {
        val json = JavaChatComponentJson.encode(TextComponent("Hello"))

        assertEquals("{\"text\":\"Hello\"}", json)
    }

    @Test
    fun escapesTextComponentJson() {
        val json = JavaChatComponentJson.encode(TextComponent("Bad \"name\"\n\u0001"))

        assertEquals("{\"text\":\"Bad \\\"name\\\"\\n\\u0001\"}", json)
    }
}
