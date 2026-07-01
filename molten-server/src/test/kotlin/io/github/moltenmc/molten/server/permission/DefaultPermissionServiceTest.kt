package io.github.moltenmc.molten.server.permission

import io.github.moltenmc.molten.api.command.CommandSource
import io.github.moltenmc.molten.api.command.CommandSourceType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultPermissionServiceTest {
    @Test
    fun resolvesExactAndWildcardPermissions() {
        val source = TestSource("operator")
        val provider = DefaultPermissionProvider(
            mapOf("operator" to setOf("molten.admin.*", "molten.server.stop")),
        )
        val service = DefaultPermissionService(provider)

        assertTrue(service.hasPermission(source, "molten.admin.reload"))
        assertTrue(service.hasPermission(source, "molten.server.stop"))
        assertFalse(service.hasPermission(source, "molten.world.delete"))
    }

    private data class TestSource(
        override val name: String,
    ) : CommandSource {
        override val sourceType: CommandSourceType = CommandSourceType.CONSOLE

        override fun sendMessage(message: String) {
        }
    }
}
