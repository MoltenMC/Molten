description = "Minecraft Java Edition protocol 776 implementation."

dependencies {
    implementation(project(":molten-api"))
    implementation(project(":molten-common"))
    implementation(project(":molten-translator"))
    implementation(libs.molten.anvil)
    implementation(libs.netty.buffer)
    implementation(libs.netty.common)
    implementation(libs.netty.transport)
}
