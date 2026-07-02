package io.github.moltenmc.molten.server.tick

import io.github.moltenmc.molten.server.network.ProtocolListener
import io.github.moltenmc.molten.server.runtime.ProtocolStack
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtocolListenerTickTaskTest {
    @Test
    fun executesNetworkEgressStepAndTicksListenerEgress() {
        val first = RecordingProtocolListener()
        val second = RecordingProtocolListener()
        val task = ProtocolListenerTickTask(listOf(first, second))

        task.execute(currentTick = 7).get()

        assertEquals(TickPipelineStep.NETWORK_EGRESS, task.step)
        assertEquals(1, first.egressTickCalls)
        assertEquals(1, second.egressTickCalls)
        assertEquals(0, first.ingressTickCalls)
    }

    @Test
    fun executesNetworkIngressStepAndTicksListenerIngress() {
        val listener = RecordingProtocolListener()
        val task = ProtocolListenerTickTask(
            listeners = listOf(listener),
            step = TickPipelineStep.NETWORK_INGRESS,
            tickListener = ProtocolListener::tickIngress,
        )

        task.execute(currentTick = 7).get()

        assertEquals(TickPipelineStep.NETWORK_INGRESS, task.step)
        assertEquals(1, listener.ingressTickCalls)
        assertEquals(0, listener.egressTickCalls)
    }

    private class RecordingProtocolListener : ProtocolListener {
        var ingressTickCalls: Int = 0
        var egressTickCalls: Int = 0

        override val protocol: ProtocolStack = ProtocolStack.JAVA_EDITION
        override val isRunning: Boolean = true

        override fun start() = Unit

        override fun tickIngress(): Int {
            ingressTickCalls++
            return 1
        }

        override fun tickEgress(): Int {
            egressTickCalls++
            return 1
        }

        override fun stop() = Unit
    }
}
