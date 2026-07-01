package io.github.moltenmc.molten.common.registry

data class RegistryKey(
    val namespace: String,
    val value: String,
) {
    init {
        require(namespace.isNotBlank()) { "Registry namespace is required." }
        require(value.isNotBlank()) { "Registry value is required." }
    }

    override fun toString(): String = "$namespace:$value"

    companion object {
        fun molten(value: String): RegistryKey = RegistryKey("molten", value)

        fun parse(value: String): RegistryKey {
            val separator = value.indexOf(':')
            require(separator > 0 && separator < value.lastIndex) {
                "Registry key must use namespace:value format."
            }
            return RegistryKey(
                namespace = value.substring(0, separator),
                value = value.substring(separator + 1),
            )
        }
    }
}
