package io.github.moltenmc.molten.server.tick

data class TickMetricsSnapshot(
    val startedTicks: Long,
    val completedTicks: Long,
    val failedTicks: Long,
    val skippedTicks: Long,
    val totalDurationNanos: Long,
    val maxDurationNanos: Long,
    val lastCompletedTick: Long?,
    val lastFailedTick: Long?,
    val lastFailure: TickFailure?,
    val skippedByReason: Map<TickSkipReason, Long>,
) {
    val averageDurationNanos: Long
        get() {
            val measuredTicks = completedTicks + failedTicks
            return if (measuredTicks == 0L) 0 else totalDurationNanos / measuredTicks
        }
}
