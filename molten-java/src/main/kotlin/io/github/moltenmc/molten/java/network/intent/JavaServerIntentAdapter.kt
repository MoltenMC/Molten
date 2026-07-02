package io.github.moltenmc.molten.java.network.intent

import io.github.moltenmc.molten.common.network.IntentRouting
import io.github.moltenmc.molten.common.network.intent.ServerIntent
import io.github.moltenmc.molten.common.spi.ProtocolAdapter
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.PlayerChatPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder

class JavaServerIntentAdapter(
    private val sessionHolder: JavaSessionHolder,
) : ProtocolAdapter<JavaPacket> {
    override fun toIntent(packet: JavaPacket): ServerIntent? =
        when (packet) {
            is PlayerChatPacket -> playerChatIntent(packet)
            else -> null
        }

    override fun fromReplication(replication: Any): JavaPacket? = null

    private fun playerChatIntent(packet: PlayerChatPacket): ServerIntent.PlayerChat? {
        val sourceEntityId = sessionHolder.playerEntityId ?: return null
        return ServerIntent.PlayerChat(
            sourceEntityId = sourceEntityId,
            routing = IntentRouting(
                worldId = sessionHolder.currentWorld,
                regionPos = sessionHolder.currentRegion,
            ),
            message = packet.message,
        )
    }
}
