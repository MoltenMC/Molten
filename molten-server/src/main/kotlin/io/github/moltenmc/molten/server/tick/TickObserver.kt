package io.github.moltenmc.molten.server.tick

interface TickObserver {
    fun onTickStarted(currentTick: Long) {
    }

    fun onTickCompleted(result: ServerTickResult.Completed, durationNanos: Long) {
    }

    fun onTickFailed(result: ServerTickResult.Failed, durationNanos: Long) {
    }

    fun onScheduledTickSkipped(reason: TickSkipReason) {
    }
}
