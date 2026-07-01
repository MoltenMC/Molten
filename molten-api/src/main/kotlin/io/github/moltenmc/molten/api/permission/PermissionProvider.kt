package io.github.moltenmc.molten.api.permission

import io.github.moltenmc.molten.api.command.CommandSource

interface PermissionProvider {
    fun resolve(source: CommandSource, node: PermissionNode, context: PermissionContext): PermissionDecision

    fun invalidate(source: CommandSource)
}
