package io.github.moltenmc.molten.api.command

enum class CommandExecutionPolicy {
    AUTHORITATIVE_REGION,
    SERVER_CONTROL_EXECUTOR,
    ASYNC_LONG_RUNNING,
}
