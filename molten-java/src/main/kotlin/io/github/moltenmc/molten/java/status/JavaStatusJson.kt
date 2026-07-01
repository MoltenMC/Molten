package io.github.moltenmc.molten.java.status

import io.github.moltenmc.molten.java.JavaEditionProtocol

object JavaStatusJson {
    fun encode(status: JavaStatusResponse): String {
        require(status.onlinePlayers >= 0) { "Online player count must not be negative." }
        require(status.maxPlayers >= 0) { "Maximum player count must not be negative." }

        return buildString {
            append('{')
            append("\"version\":{")
            append("\"name\":\"Molten Java 25\",")
            append("\"protocol\":")
            append(JavaEditionProtocol.PROTOCOL_VERSION)
            append("},")
            append("\"players\":{")
            append("\"max\":")
            append(status.maxPlayers)
            append(',')
            append("\"online\":")
            append(status.onlinePlayers)
            append("},")
            append("\"description\":{")
            append("\"text\":\"")
            append(status.description.escapeJson())
            append("\"}")
            append('}')
        }
    }

    private fun String.escapeJson(): String =
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
