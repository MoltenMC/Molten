package io.github.moltenmc.molten.server.world

import java.nio.file.Path

data class WorldStoragePaths(
    val worldDirectory: Path,
    val javaRegionDirectory: Path = worldDirectory.resolve("region"),
    val bedrockDatabasePath: Path = worldDirectory.resolve("db"),
)
