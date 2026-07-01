package io.github.moltenmc.molten.bedrock.network.session

import io.github.moltenmc.molten.bedrock.network.raknet.RakNetEndpoint
import io.github.moltenmc.molten.bedrock.protocol.BedrockProtocolState
import io.github.moltenmc.molten.common.ecs.EntityId
import io.github.moltenmc.molten.common.region.RegionPos
import io.github.moltenmc.molten.common.world.WorldId
import java.net.SocketAddress
import java.util.UUID

data class BedrockSession(
    val connectionId: UUID,
    val raknetPeer: RakNetEndpoint?,
    val remoteAddress: SocketAddress?,
    val protocolVersion: Int,
    val xuid: String? = null,
    val profile: BedrockSessionProfile? = null,
    val encryptionState: BedrockEncryptionState = BedrockEncryptionState.DISABLED,
    val resourcePackState: ResourcePackState = ResourcePackState.NOT_STARTED,
    val playerEntityId: EntityId? = null,
    val currentWorld: WorldId? = null,
    val currentRegion: RegionPos? = null,
    val capabilities: Set<BedrockClientCapability> = emptySet(),
    val state: BedrockProtocolState = BedrockProtocolState.RAKNET_CONNECTING,
)
