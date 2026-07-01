package io.github.moltenmc.molten.server.tick

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class ServerTickLoop(
    private val pipeline: TickPipeline,
    private val failurePolicy: TickFailurePolicy = TickFailurePolicy.StopLoop,
    private val observers: List<TickObserver> = emptyList(),
) {
    private val runningRef = AtomicBoolean(false)
    private val tickingRef = AtomicBoolean(false)
    private val nextTickRef = AtomicLong(0)
    private val scheduleRef = AtomicReference<ScheduledTickHandle?>(null)

    val isRunning: Boolean
        get() = runningRef.get()

    val nextTick: Long
        get() = nextTickRef.get()

    fun start() {
        runningRef.set(true)
    }

    fun stop() {
        runningRef.set(false)
        scheduleRef.getAndSet(null)?.cancel()
    }

    fun startScheduled(
        tickRate: TickRate = TickRate.MinecraftDefault,
    ): ScheduledTickHandle =
        startScheduled(
            tickRate = tickRate,
            executor = Executors.newSingleThreadScheduledExecutor { runnable ->
                Thread(runnable, "molten-server-tick").apply {
                    isDaemon = true
                }
            },
            ownsExecutor = true,
        )

    fun startScheduled(
        tickRate: TickRate,
        executor: ScheduledExecutorService,
    ): ScheduledTickHandle =
        startScheduled(tickRate, executor, ownsExecutor = false)

    fun tickOnce(): CompletableFuture<ServerTickResult> {
        if (!runningRef.get()) {
            return CompletableFuture.failedFuture(IllegalStateException("Tick loop is not running."))
        }

        val currentTick = nextTickRef.getAndIncrement()
        val startedAt = System.nanoTime()
        notifyObservers { observer -> observer.onTickStarted(currentTick) }
        return pipeline.execute(currentTick)
            .thenApply<ServerTickResult> { result ->
                ServerTickResult.Completed(currentTick, result).also { completed ->
                    notifyObservers { observer ->
                        observer.onTickCompleted(completed, System.nanoTime() - startedAt)
                    }
                }
            }
            .exceptionally { error ->
                val failure = error.toTickFailure(currentTick)
                val action = failurePolicy.onFailure(failure)
                if (action == TickFailureAction.STOP) {
                    stop()
                }
                ServerTickResult.Failed(currentTick, failure, action).also { failed ->
                    notifyObservers { observer ->
                        observer.onTickFailed(failed, System.nanoTime() - startedAt)
                    }
                }
            }
    }

    private fun startScheduled(
        tickRate: TickRate,
        executor: ScheduledExecutorService,
        ownsExecutor: Boolean,
    ): ScheduledTickHandle {
        start()
        check(scheduleRef.get() == null) { "Tick loop is already scheduled." }

        val future = executor.scheduleAtFixedRate(
            { runScheduledTick() },
            0,
            tickRate.periodNanos,
            TimeUnit.NANOSECONDS,
        )
        val handle = ScheduledTickHandle(future, executor, ownsExecutor)
        if (!scheduleRef.compareAndSet(null, handle)) {
            handle.cancel()
            error("Tick loop is already scheduled.")
        }
        return handle
    }

    private fun runScheduledTick() {
        if (!runningRef.get()) {
            notifyObservers { observer -> observer.onScheduledTickSkipped(TickSkipReason.NOT_RUNNING) }
            return
        }
        if (!tickingRef.compareAndSet(false, true)) {
            notifyObservers { observer -> observer.onScheduledTickSkipped(TickSkipReason.PREVIOUS_TICK_RUNNING) }
            return
        }

        tickOnce().whenComplete { _, _ ->
            tickingRef.set(false)
        }
    }

    private fun Throwable.toTickFailure(currentTick: Long): TickFailure {
        val unwrapped = cause ?: this
        if (unwrapped is TickPipelineException) {
            return unwrapped.failure
        }
        return TickFailure(
            currentTick = currentTick,
            failedStep = TickPipelineStep.CLEANUP,
            executedSteps = emptyList(),
            cause = unwrapped,
        )
    }

    private fun notifyObservers(action: (TickObserver) -> Unit) {
        observers.forEach { observer ->
            try {
                action(observer)
            } catch (_: RuntimeException) {
            }
        }
    }
}
