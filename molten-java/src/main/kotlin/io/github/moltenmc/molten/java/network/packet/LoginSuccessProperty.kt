package io.github.moltenmc.molten.java.network.packet

data class LoginSuccessProperty(
    val name: String,
    val value: String,
    val signature: String?,
)
