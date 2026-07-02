package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.server.network.ProtocolListener
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtocolListenerTickTaskTest {
    @Test
    fun executesNetworkEgressStepAndTicksListeners() {
        val first = RecordingProtocolListener()
        val second = RecordingProtocolListener()
        val task = ProtocolListenerTickTask(listOf(first, second))

        task.execute(currentTick = 7).get()

        assertEquals(TickPipelineStep.NETWORK_EGRESS, task.step)
        assertEquals(1, first.tickCalls)
        assertEquals(1, second.tickCalls)
    }

    private class RecordingProtocolListener : ProtocolListener {
        var tickCalls: Int = 0

        override val protocol: ProtocolStack = ProtocolStack.JAVA_EDITION
        override val isRunning: Boolean = true

        override fun start() = Unit

        override fun tick(): Int {
            tickCalls++
            return 1
        }

        override fun stop() = Unit
    }
}
