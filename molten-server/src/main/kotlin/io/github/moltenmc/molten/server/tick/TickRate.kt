package io.github.moltenmc.molten.server.tick

data class TickRate(
    val ticksPerSecond: Int,
) {
    init {
        require(ticksPerSecond > 0) { "Ticks per second must be positive." }
    }

    val periodNanos: Long = NANOS_PER_SECOND / ticksPerSecond

    companion object {
        private const val NANOS_PER_SECOND = 1_000_000_000L

        val MinecraftDefault: TickRate = TickRate(20)
    }
}
