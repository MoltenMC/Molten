package io.github.moltenmc.molten.java.status

import io.github.moltenmc.molten.common.text.ChatComponent
import io.github.moltenmc.molten.common.text.TextComponent

object JavaChatComponentJson {
    fun encode(component: ChatComponent): String =
        when (component) {
            is TextComponent -> "{\"text\":\"${component.plainText.escapeJson()}\"}"
        }

    fun String.escapeJson(): String =
        buildString {
            this@escapeJson.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (character.code < 0x20) {
                            append("\\u")
                            append(character.code.toString(16).padStart(4, '0'))
                        } else {
                            append(character)
                        }
                    }
                }
            }
        }
}
