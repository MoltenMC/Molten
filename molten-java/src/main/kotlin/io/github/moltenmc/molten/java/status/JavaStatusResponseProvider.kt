package io.github.moltenmc.molten.java.status

fun interface JavaStatusResponseProvider {
    fun currentStatus(): JavaStatusResponse
}
