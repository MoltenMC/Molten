package io.github.moltenmc.molten.java.network.handler

import io.github.moltenmc.molten.java.network.codec.JavaPlayJoinPacketCodec
import io.github.moltenmc.molten.java.network.packet.JavaPacket
import io.github.moltenmc.molten.java.network.packet.JavaPlayJoinPacket
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.protocol.JavaProtocolState

class JavaPlayStartHandler {
    fun playPacketsFor(sessionHolder: JavaSessionHolder): List<JavaPacket> {
        require(sessionHolder.state == JavaProtocolState.PLAY) {
            "Play packets can only be created in PLAY state."
        }
        return listOf(
            JavaPlayJoinPacket(
                packetId = JavaPlayJoinPacketCodec.PACKET_ID,
                entityId = DEFAULT_ENTITY_ID,
                hardcore = false,
                gameMode = SURVIVAL_GAME_MODE,
                previousGameMode = NO_PREVIOUS_GAME_MODE,
                dimensionType = "minecraft:overworld",
                worldName = "minecraft:overworld",
                hashedSeed = 0L,
            ),
        )
    }

    companion object {
        private const val DEFAULT_ENTITY_ID = 1
        private const val SURVIVAL_GAME_MODE = 0
        private const val NO_PREVIOUS_GAME_MODE = -1
    }
}
