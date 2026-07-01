package io.github.moltenmc.molten.java.network.session

import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import io.github.moltenmc.molten.java.protocol.JavaProtocolState
import java.net.SocketAddress
import java.util.UUID

data class JavaSession(
    val connectionId: UUID,
    val remoteAddress: SocketAddress?,
    val protocolVersion: Int,
    val profile: JavaSessionProfile? = null,
    val compressionState: JavaCompressionState = JavaCompressionState.DISABLED,
    val encryptionState: JavaEncryptionState = JavaEncryptionState.DISABLED,
    val playerEntityId: EntityId? = null,
    val currentWorld: WorldId? = null,
    val currentRegion: RegionPos? = null,
    val capabilities: Set<JavaClientCapability> = emptySet(),
    val state: JavaProtocolState = JavaProtocolState.HANDSHAKE,
)
