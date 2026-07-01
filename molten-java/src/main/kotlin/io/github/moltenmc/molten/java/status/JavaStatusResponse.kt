package io.github.moltenmc.molten.java.status

data class JavaStatusResponse(
    val description: String,
    val onlinePlayers: Int,
    val maxPlayers: Int,
)
