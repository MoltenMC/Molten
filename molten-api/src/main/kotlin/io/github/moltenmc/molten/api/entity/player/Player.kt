package io.github.moltenmc.molten.api.entity.player

import io.github.moltenmc.molten.api.command.CommandSource
import io.github.moltenmc.molten.api.entity.EntityView
import java.util.UUID

interface Player : CommandSource, EntityView {
    val uniqueId: UUID

    val protocolFamily: ProtocolFamily
}
