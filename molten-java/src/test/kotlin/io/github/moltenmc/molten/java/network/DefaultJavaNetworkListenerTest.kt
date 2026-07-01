package io.github.moltenmc.molten.java.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultJavaNetworkListenerTest {
    @Test
    fun bindsToLocalRandomPort() {
        val listener = DefaultJavaNetworkListener()

        try {
            listener.bind("127.0.0.1", 0)

            assertTrue(listener.isBound)
            assertNotNull(listener.localAddress)
            assertTrue(listener.localAddress!!.port > 0)
        } finally {
            listener.close()
        }
    }

    @Test
    fun rejectsDuplicateBind() {
        val listener = DefaultJavaNetworkListener()

        try {
            listener.bind("127.0.0.1", 0)

            assertFailsWith<IllegalStateException> {
                listener.bind("127.0.0.1", 0)
            }
        } finally {
            listener.close()
        }
    }

    @Test
    fun closeIsIdempotent() {
        val listener = DefaultJavaNetworkListener()

        listener.bind("127.0.0.1", 0)
        listener.close()
        listener.close()

        assertFalse(listener.isBound)
        assertEquals(null, listener.localAddress)
    }

    @Test
    fun canRebindAfterClose() {
        val listener = DefaultJavaNetworkListener()

        try {
            listener.bind("127.0.0.1", 0)
            listener.close()
            listener.bind("127.0.0.1", 0)

            assertTrue(listener.isBound)
        } finally {
            listener.close()
        }
    }
}
