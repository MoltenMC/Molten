package io.github.moltenmc.molten.api.plugin

import io.github.moltenmc.molten.api.command.CommandRegistry
import io.github.moltenmc.molten.api.event.EventBus
import io.github.moltenmc.molten.api.scheduler.Scheduler

interface PluginContext {
    val commands: CommandRegistry

    val events: EventBus

    val scheduler: Scheduler
}
