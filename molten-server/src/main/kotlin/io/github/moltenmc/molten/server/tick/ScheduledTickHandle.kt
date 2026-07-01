package io.github.moltenmc.molten.server.tick

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture

class ScheduledTickHandle internal constructor(
    private val future: ScheduledFuture<*>,
    private val executor: ScheduledExecutorService,
    private val ownsExecutor: Boolean,
) {
    val isCancelled: Boolean
        get() = future.isCancelled

    fun cancel() {
        future.cancel(false)
        if (ownsExecutor) {
            executor.shutdown()
        }
    }
}
