package io.github.moltenmc.molten.common.scheduler

import java.time.Duration
import java.util.concurrent.CompletableFuture

interface TaskScheduler : AutoCloseable {
    fun execute(task: Runnable): CompletableFuture<Void>

    fun schedule(delay: Duration, task: Runnable): CompletableFuture<Void>

    override fun close()
}
