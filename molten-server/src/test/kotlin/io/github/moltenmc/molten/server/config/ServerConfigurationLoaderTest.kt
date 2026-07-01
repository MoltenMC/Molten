package io.github.moltenmc.molten.server.config

import io.github.moltenmc.molten.server.ServerConfiguration
import io.github.moltenmc.molten.server.runtime.RuntimeMode
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ServerConfigurationLoaderTest {
    @Test
    fun returnsDefaultsWhenConfigFilesAreMissing() {
        withTempDirectory { directory ->
            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals(ServerConfiguration.defaults(), configuration)
        }
    }

    @Test
    fun loadOrCreateWritesBothDefaultConfigFilesWhenMissing() {
        withTempDirectory { directory ->
            val configuration = ServerConfigurationLoader(directory).loadOrCreate()

            assertEquals(ServerConfiguration.defaults(), configuration)
            assertTrue(Files.exists(directory.resolve("server.properties")))
            assertTrue(Files.exists(directory.resolve("molten.yaml")))
            assertEquals(
                """
                bind-address=0.0.0.0
                java-port=25565
                bedrock-port=19132
                tick-rate=20
                """.trimIndent() + "\n",
                Files.readString(directory.resolve("server.properties")),
            )
            assertEquals(
                """
                runtime-mode: java-based
                world-directory: world
                """.trimIndent() + "\n",
                Files.readString(directory.resolve("molten.yaml")),
            )
        }
    }

    @Test
    fun loadOrCreateDoesNotOverwriteExistingFiles() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("server.properties"),
                """
                bind-address=127.0.0.1
                java-port=25566
                """.trimIndent(),
            )
            Files.writeString(
                directory.resolve("molten.yaml"),
                """
                runtime-mode: bedrock-only
                world-directory: custom-world
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).loadOrCreate()

            assertEquals("127.0.0.1", configuration.bindAddress)
            assertEquals(25566, configuration.javaPort)
            assertEquals(RuntimeMode.BEDROCK_ONLY, configuration.runtimeMode)
            assertEquals(Path.of("custom-world"), configuration.worldDirectory)
            assertEquals(
                """
                bind-address=127.0.0.1
                java-port=25566
                """.trimIndent(),
                Files.readString(directory.resolve("server.properties")),
            )
        }
    }

    @Test
    fun loadOrCreateCreatesOnlyMissingConfigFile() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("server.properties"),
                "java-port=25566",
            )

            val configuration = ServerConfigurationLoader(directory).loadOrCreate()

            assertEquals(25566, configuration.javaPort)
            assertEquals(ServerConfiguration.defaults().runtimeMode, configuration.runtimeMode)
            assertTrue(Files.exists(directory.resolve("molten.yaml")))
        }
    }

    @Test
    fun loadsBasicSettingsFromServerProperties() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("server.properties"),
                """
                bind-address=127.0.0.1
                java-port=25566
                bedrock-port=19133
                tick-rate=30
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals("127.0.0.1", configuration.bindAddress)
            assertEquals(25566, configuration.javaPort)
            assertEquals(19133, configuration.bedrockPort)
            assertEquals(30, configuration.tickRate.ticksPerSecond)
        }
    }

    @Test
    fun supportsMinecraftStyleServerPropertiesAliases() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("server.properties"),
                """
                server-ip=127.0.0.2
                server-port=25567
                tps=40
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals("127.0.0.2", configuration.bindAddress)
            assertEquals(25567, configuration.javaPort)
            assertEquals(40, configuration.tickRate.ticksPerSecond)
        }
    }

    @Test
    fun loadsMoltenSettingsFromMoltenYaml() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("molten.yaml"),
                """
                runtime-mode: bedrock-only
                world-directory: "bedrock-world"
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals(RuntimeMode.BEDROCK_ONLY, configuration.runtimeMode)
            assertEquals(Path.of("bedrock-world"), configuration.worldDirectory)
        }
    }

    @Test
    fun combinesServerPropertiesAndMoltenYaml() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("server.properties"),
                """
                bind-address=127.0.0.1
                java-port=25566
                """.trimIndent(),
            )
            Files.writeString(
                directory.resolve("molten.yaml"),
                """
                runtimeMode: java-only
                worldDirectory: java-world
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals("127.0.0.1", configuration.bindAddress)
            assertEquals(25566, configuration.javaPort)
            assertEquals(RuntimeMode.JAVA_ONLY, configuration.runtimeMode)
            assertEquals(Path.of("java-world"), configuration.worldDirectory)
        }
    }

    @Test
    fun ignoresMoltenOnlyKeysInServerProperties() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("server.properties"),
                """
                runtime-mode=bedrock-only
                world-directory=ignored-world
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals(ServerConfiguration.defaults().runtimeMode, configuration.runtimeMode)
            assertEquals(ServerConfiguration.defaults().worldDirectory, configuration.worldDirectory)
        }
    }

    @Test
    fun ignoresBasicKeysInMoltenYaml() {
        withTempDirectory { directory ->
            Files.writeString(
                directory.resolve("molten.yaml"),
                """
                java-port: 25566
                tick-rate: 30
                """.trimIndent(),
            )

            val configuration = ServerConfigurationLoader(directory).load()

            assertEquals(ServerConfiguration.defaults().javaPort, configuration.javaPort)
            assertEquals(ServerConfiguration.defaults().tickRate, configuration.tickRate)
        }
    }

    @Test
    fun rejectsInvalidServerPropertiesNumber() {
        withTempDirectory { directory ->
            Files.writeString(directory.resolve("server.properties"), "java-port=abc")

            assertFailsWith<IllegalArgumentException> {
                ServerConfigurationLoader(directory).load()
            }
        }
    }

    @Test
    fun rejectsInvalidMoltenYamlRuntimeMode() {
        withTempDirectory { directory ->
            Files.writeString(directory.resolve("molten.yaml"), "runtime-mode: invalid")

            assertFailsWith<IllegalArgumentException> {
                ServerConfigurationLoader(directory).load()
            }
        }
    }

    @Test
    fun rejectsInvalidMoltenYamlShape() {
        withTempDirectory { directory ->
            Files.writeString(directory.resolve("molten.yaml"), "runtime-mode")

            assertFailsWith<IllegalArgumentException> {
                ServerConfigurationLoader(directory).load()
            }
        }
    }

    private fun withTempDirectory(block: (Path) -> Unit) {
        val directory = Files.createTempDirectory("molten-config-loader-test")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
