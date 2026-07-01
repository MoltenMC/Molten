package io.github.moltenmc.molten.server.tick

import java.util.EnumMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder

class InMemoryTickMetricsObserver : TickObserver {
    private val startedTicks = LongAdder()
    private val completedTicks = LongAdder()
    private val failedTicks = LongAdder()
    private val skippedTicks = LongAdder()
    private val totalDurationNanos = LongAdder()
    private val maxDurationNanos = AtomicLong(0)
    private val lastCompletedTick = AtomicReference<Long?>(null)
    private val lastFailedTick = AtomicReference<Long?>(null)
    private val lastFailure = AtomicReference<TickFailure?>(null)
    private val skippedByReason = EnumMap<TickSkipReason, LongAdder>(TickSkipReason::class.java)

    init {
        TickSkipReason.entries.forEach { reason ->
            skippedByReason[reason] = LongAdder()
        }
    }

    override fun onTickStarted(currentTick: Long) {
        startedTicks.increment()
    }

    override fun onTickCompleted(result: ServerTickResult.Completed, durationNanos: Long) {
        completedTicks.increment()
        totalDurationNanos.add(durationNanos)
        maxDurationNanos.accumulateAndGet(durationNanos, ::maxOf)
        lastCompletedTick.set(result.currentTick)
    }

    override fun onTickFailed(result: ServerTickResult.Failed, durationNanos: Long) {
        failedTicks.increment()
        totalDurationNanos.add(durationNanos)
        maxDurationNanos.accumulateAndGet(durationNanos, ::maxOf)
        lastFailedTick.set(result.currentTick)
        lastFailure.set(result.failure)
    }

    override fun onScheduledTickSkipped(reason: TickSkipReason) {
        skippedTicks.increment()
        skippedByReason.getValue(reason).increment()
    }

    fun snapshot(): TickMetricsSnapshot =
        TickMetricsSnapshot(
            startedTicks = startedTicks.sum(),
            completedTicks = completedTicks.sum(),
            failedTicks = failedTicks.sum(),
            skippedTicks = skippedTicks.sum(),
            totalDurationNanos = totalDurationNanos.sum(),
            maxDurationNanos = maxDurationNanos.get(),
            lastCompletedTick = lastCompletedTick.get(),
            lastFailedTick = lastFailedTick.get(),
            lastFailure = lastFailure.get(),
            skippedByReason = skippedByReason.mapValues { (_, count) -> count.sum() },
        )
}
