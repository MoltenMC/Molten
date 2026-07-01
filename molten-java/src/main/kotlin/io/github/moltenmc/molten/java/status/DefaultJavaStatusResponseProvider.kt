package io.github.moltenmc.molten.java.status

class DefaultJavaStatusResponseProvider(
    private val description: String = "A Molten server",
    private val onlinePlayers: Int = 0,
    private val maxPlayers: Int = 20,
) : JavaStatusResponseProvider {
    override fun currentStatus(): JavaStatusResponse =
        JavaStatusResponse(
            description = description,
            onlinePlayers = onlinePlayers,
            maxPlayers = maxPlayers,
        )
}
