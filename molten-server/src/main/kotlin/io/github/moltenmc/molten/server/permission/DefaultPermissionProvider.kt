package io.github.moltenmc.molten.server.permission

import io.github.moltenmc.molten.api.command.CommandSource
import io.github.moltenmc.molten.api.permission.PermissionContext
import io.github.moltenmc.molten.api.permission.PermissionDecision
import io.github.moltenmc.molten.api.permission.PermissionNode
import io.github.moltenmc.molten.api.permission.PermissionProvider

class DefaultPermissionProvider(
    private val permissionsBySource: Map<String, Set<String>> = emptyMap(),
) : PermissionProvider {
    override fun resolve(
        source: CommandSource,
        node: PermissionNode,
        context: PermissionContext,
    ): PermissionDecision {
        val permissions = permissionsBySource[source.name].orEmpty()
        return when {
            node.value in permissions -> PermissionDecision.ALLOW
            permissions.any { wildcardMatches(it, node.value) } -> PermissionDecision.ALLOW
            else -> PermissionDecision.UNSET
        }
    }

    override fun invalidate(source: CommandSource) {
    }

    private fun wildcardMatches(pattern: String, permission: String): Boolean {
        if (!pattern.endsWith(".*")) {
            return false
        }
        return permission.startsWith(pattern.removeSuffix(".*") + ".")
    }
}
