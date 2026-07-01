package io.github.moltenmc.molten.server.service

import io.github.moltenmc.molten.api.permission.PermissionService

data class CoreServices(
    val permissionService: PermissionService,
)
