package io.github.moltenmc.molten.server.tick

fun interface TickFailurePolicy {
    fun onFailure(failure: TickFailure): TickFailureAction

    companion object {
        val StopLoop: TickFailurePolicy = TickFailurePolicy { TickFailureAction.STOP }
        val ContinueLoop: TickFailurePolicy = TickFailurePolicy { TickFailureAction.CONTINUE }
    }
}
