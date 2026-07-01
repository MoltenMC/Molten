package io.github.moltenmc.molten.api.permission

data class PermissionContext(
    val world: String? = null,
    val region: String? = null,
    val dimension: String? = null,
    val runtime: String? = null,
    val commandSource: String? = null,
    val time: String? = null,
    val serverEnvironment: String? = null,
)
