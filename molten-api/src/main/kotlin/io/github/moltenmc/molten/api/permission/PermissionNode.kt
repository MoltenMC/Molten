package io.github.moltenmc.molten.api.permission

@JvmInline
value class PermissionNode(val value: String) {
    init {
        require(value.isNotBlank()) { "Permission node is required." }
    }
}
