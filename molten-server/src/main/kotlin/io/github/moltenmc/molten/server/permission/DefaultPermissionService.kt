package io.github.moltenmc.molten.server.permission

import io.github.moltenmc.molten.api.command.CommandSource
import io.github.moltenmc.molten.api.permission.PermissionContext
import io.github.moltenmc.molten.api.permission.PermissionDecision
import io.github.moltenmc.molten.api.permission.PermissionNode
import io.github.moltenmc.molten.api.permission.PermissionProvider
import io.github.moltenmc.molten.api.permission.PermissionService

class DefaultPermissionService(
    private val provider: PermissionProvider,
) : PermissionService {
    override fun hasPermission(source: CommandSource, permission: String): Boolean =
        hasPermission(source, PermissionNode(permission), PermissionContext(commandSource = source.name))

    override fun hasPermission(
        source: CommandSource,
        permission: PermissionNode,
        context: PermissionContext,
    ): Boolean = provider.resolve(source, permission, context) == PermissionDecision.ALLOW
}
