package io.github.moltenmc.molten.common.world

import java.util.UUID

@JvmInline
value class WorldId(val value: UUID) {
    companion object {
        fun random(): WorldId = WorldId(UUID.randomUUID())
    }
}
