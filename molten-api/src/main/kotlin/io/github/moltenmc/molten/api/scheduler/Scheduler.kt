package io.github.moltenmc.molten.api.scheduler

import java.time.Duration
import java.util.concurrent.CompletableFuture

interface Scheduler {
    fun async(task: Runnable): CompletableFuture<Void>

    fun later(delay: Duration, task: Runnable): CompletableFuture<Void>
}
