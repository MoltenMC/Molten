package io.github.moltenmc.molten.api.permission

import io.github.moltenmc.molten.api.command.CommandSource

interface PermissionService {
    fun hasPermission(source: CommandSource, permission: String): Boolean

    fun hasPermission(source: CommandSource, permission: PermissionNode, context: PermissionContext): Boolean
}
