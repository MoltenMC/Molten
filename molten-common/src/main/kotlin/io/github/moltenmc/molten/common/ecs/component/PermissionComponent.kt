package io.github.moltenmc.molten.common.ecs.component

import io.github.moltenmc.molten.common.ecs.Component

data class PermissionComponent(
    val subjectId: String,
    val cachedPermissionsVersion: Long,
) : Component
