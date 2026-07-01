package io.github.moltenmc.molten.common.spi

interface PermissionProvider<Subject> {
    fun hasPermission(subject: Subject, permission: String, context: Map<String, String>): Boolean

    fun invalidate(subject: Subject)
}
