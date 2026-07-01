description = "Minecraft Bedrock Edition protocol 1001 implementation."

dependencies {
    implementation(project(":molten-api"))
    implementation(project(":molten-common"))
    implementation(project(":molten-translator"))
    implementation(libs.molten.leveldb)
    implementation(libs.raknetty.core)
    implementation(libs.raknetty.codec)
    implementation(libs.raknetty.handler)
    implementation(libs.raknetty.transport)
}
