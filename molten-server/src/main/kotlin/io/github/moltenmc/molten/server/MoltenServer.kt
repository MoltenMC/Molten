package io.github.moltenmc.molten.server

import java.util.concurrent.atomic.AtomicReference

class MoltenServer(
    val configuration: ServerConfiguration,
) {
    private val stateRef = AtomicReference(LifecycleState.CREATED)

    val state: LifecycleState
        get() = stateRef.get()

    fun start() {
        if (stateRef.compareAndSet(LifecycleState.CREATED, LifecycleState.STARTING)) {
            stateRef.set(LifecycleState.RUNNING)
        }
    }

    fun stop() {
        val current = stateRef.get()
        if (current == LifecycleState.STOPPED || current == LifecycleState.STOPPING) {
            return
        }
        stateRef.set(LifecycleState.STOPPING)
        stateRef.set(LifecycleState.STOPPED)
    }
}

fun main() {
    val server = MoltenServer(ServerConfiguration.defaults())
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(server::stop, "molten-shutdown"))
}
