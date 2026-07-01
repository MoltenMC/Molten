package io.github.moltenmc.molten.server

import io.github.moltenmc.molten.common.world.WorldStorageKind
import io.github.moltenmc.molten.server.runtime.RuntimeMode
import java.nio.file.Path

data class ServerStartupSummary(
    val bindAddress: String,
    val javaPort: Int,
    val bedrockPort: Int,
    val tickRate: Int,
    val runtimeMode: RuntimeMode,
    val worldDirectory: Path,
    val storageKind: WorldStorageKind?,
) {
    fun lines(): List<String> =
        listOf(
            "Starting Molten server",
            "Bind address: $bindAddress",
            "Java port: $javaPort",
            "Bedrock port: $bedrockPort",
            "Tick rate: $tickRate TPS",
            "Runtime mode: $runtimeMode",
            "World directory: $worldDirectory",
            "World storage: ${storageKind ?: "unconfigured"}",
        )

    companion object {
        fun from(
            configuration: ServerConfiguration,
            storageKind: WorldStorageKind? = null,
        ): ServerStartupSummary =
            ServerStartupSummary(
                bindAddress = configuration.bindAddress,
                javaPort = configuration.javaPort,
                bedrockPort = configuration.bedrockPort,
                tickRate = configuration.tickRate.ticksPerSecond,
                runtimeMode = configuration.runtimeMode,
                worldDirectory = configuration.worldDirectory,
                storageKind = storageKind,
            )
    }
}
