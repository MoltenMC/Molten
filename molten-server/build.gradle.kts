plugins {
    application
}

description = "Main server bootstrap, runtime lifecycle, configuration, and plugin loading."

dependencies {
    implementation(project(":molten-api"))
    implementation(project(":molten-common"))
    implementation(project(":molten-java"))
    implementation(project(":molten-bedrock"))
    implementation(project(":molten-translator"))
}

application {
    mainClass = "io.github.moltenmc.molten.server.MoltenServerKt"
}
