package io.github.moltenmc.molten.server.config

import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.RuntimeMode
import io.github.moltenmc.molten.server.tick.TickRate
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

class ServerConfigurationLoader(
    private val directory: Path,
) {
    fun loadOrCreate(): ServerConfiguration {
        Files.createDirectories(directory)
        createIfMissing(directory.resolve(SERVER_PROPERTIES), defaultServerProperties())
        createIfMissing(directory.resolve(MOLTEN_YAML), defaultMoltenYaml())
        return load()
    }

    fun load(): ServerConfiguration {
        val defaults = ServerConfiguration.defaults()
        val serverProperties = loadServerProperties(directory.resolve(SERVER_PROPERTIES))
        val moltenYaml = loadMoltenYaml(directory.resolve(MOLTEN_YAML))

        return defaults.copy(
            bindAddress = serverProperties.basicString("bind-address")
                ?: serverProperties.basicString("server-ip")
                ?: defaults.bindAddress,
            javaPort = serverProperties.basicInt("java-port")
                ?: serverProperties.basicInt("server-port")
                ?: defaults.javaPort,
            bedrockPort = serverProperties.basicInt("bedrock-port") ?: defaults.bedrockPort,
            tickRate = serverProperties.basicInt("tick-rate")
                ?.let(::TickRate)
                ?: serverProperties.basicInt("tps")?.let(::TickRate)
                ?: defaults.tickRate,
            runtimeMode = moltenYaml.moltenRuntimeMode("runtime-mode")
                ?: moltenYaml.moltenRuntimeMode("runtimeMode")
                ?: defaults.runtimeMode,
            worldDirectory = moltenYaml.moltenPath("world-directory")
                ?: moltenYaml.moltenPath("worldDirectory")
                ?: defaults.worldDirectory,
        )
    }

    private fun loadServerProperties(path: Path): Map<String, String> {
        if (!Files.exists(path)) {
            return emptyMap()
        }

        val properties = Properties()
        Files.newInputStream(path).use(properties::load)
        return properties.stringPropertyNames()
            .associateWith { key -> properties.getProperty(key).trim() }
    }

    private fun createIfMissing(path: Path, content: String) {
        if (Files.exists(path)) {
            return
        }

        Files.createDirectories(path.parent)
        Files.writeString(path, content)
    }

    private fun loadMoltenYaml(path: Path): Map<String, String> {
        if (!Files.exists(path)) {
            return emptyMap()
        }

        return Files.readAllLines(path)
            .mapIndexedNotNull { index, line -> parseYamlLine(path, index + 1, line) }
            .toMap()
    }

    private fun parseYamlLine(path: Path, lineNumber: Int, line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }

        val separator = trimmed.indexOf(':')
        require(separator > 0) {
            "Invalid molten.yaml entry at $path:$lineNumber. Expected 'key: value'."
        }

        val key = trimmed.substring(0, separator).trim()
        val value = trimmed.substring(separator + 1).trim().stripYamlQuotes()
        require(key.isNotBlank()) {
            "Invalid molten.yaml entry at $path:$lineNumber. Key is required."
        }
        require(value.isNotBlank()) {
            "Invalid molten.yaml entry at $path:$lineNumber. Value is required."
        }
        return key to value
    }

    private fun Map<String, String>.basicString(key: String): String? =
        get(key)?.takeIf(String::isNotBlank)

    private fun Map<String, String>.basicInt(key: String): Int? =
        get(key)?.toIntOrNull()
            ?: get(key)?.let { value ->
                throw IllegalArgumentException("Invalid numeric value for $key: $value")
            }

    private fun Map<String, String>.moltenRuntimeMode(key: String): RuntimeMode? =
        get(key)?.let { value ->
            runCatching { RuntimeMode.valueOf(value.uppercase().replace('-', '_')) }
                .getOrElse { throw IllegalArgumentException("Invalid runtime mode: $value") }
        }

    private fun Map<String, String>.moltenPath(key: String): Path? =
        get(key)?.takeIf(String::isNotBlank)?.let(Path::of)

    private fun String.stripYamlQuotes(): String =
        if ((startsWith('"') && endsWith('"')) || (startsWith('\'') && endsWith('\''))) {
            substring(1, length - 1)
        } else {
            this
        }

    private fun defaultServerProperties(): String =
        """
        bind-address=${ServerConfiguration.defaults().bindAddress}
        java-port=${ServerConfiguration.defaults().javaPort}
        bedrock-port=${ServerConfiguration.defaults().bedrockPort}
        tick-rate=${ServerConfiguration.defaults().tickRate.ticksPerSecond}
        """.trimIndent() + "\n"

    private fun defaultMoltenYaml(): String =
        """
        runtime-mode: java-based
        world-directory: ${ServerConfiguration.defaults().worldDirectory}
        """.trimIndent() + "\n"

    companion object {
        const val SERVER_PROPERTIES: String = "server.properties"
        const val MOLTEN_YAML: String = "molten.yaml"
    }
}
