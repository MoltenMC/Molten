plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Molten"

include(
    "molten-api",
    "molten-common",
    "molten-server",
    "molten-java",
    "molten-bedrock",
    "molten-translator",
)
