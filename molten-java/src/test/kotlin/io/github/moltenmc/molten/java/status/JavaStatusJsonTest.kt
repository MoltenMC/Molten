package io.github.moltenmc.molten.java.status

import io.github.moltenmc.molten.java.JavaEditionProtocol
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class JavaStatusJsonTest {
    @Test
    fun encodesStatusResponseJson() {
        val json = JavaStatusJson.encode(
            JavaStatusResponse(
                description = "A Molten server",
                onlinePlayers = 3,
                maxPlayers = 20,
            ),
        )

        assertContains(json, "\"name\":\"Molten Java 25\"")
        assertContains(json, "\"protocol\":${JavaEditionProtocol.PROTOCOL_VERSION}")
        assertContains(json, "\"max\":20")
        assertContains(json, "\"online\":3")
        assertContains(json, "\"description\":{\"text\":\"A Molten server\"}")
    }

    @Test
    fun escapesDescriptionText() {
        val json = JavaStatusJson.encode(
            JavaStatusResponse(
                description = "Molten \"test\"\nserver",
                onlinePlayers = 0,
                maxPlayers = 20,
            ),
        )

        assertContains(json, "Molten \\\"test\\\"\\nserver")
    }

    @Test
    fun rejectsNegativePlayerCounts() {
        assertFailsWith<IllegalArgumentException> {
            JavaStatusJson.encode(
                JavaStatusResponse(
                    description = "A Molten server",
                    onlinePlayers = -1,
                    maxPlayers = 20,
                ),
            )
        }
    }
}
