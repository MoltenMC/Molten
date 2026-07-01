package io.github.moltenmc.molten.common.world.section

data class LightData(
    val blockLight: ByteArray,
    val skyLight: ByteArray,
) {
    override fun equals(other: Any?): Boolean =
        other is LightData &&
            blockLight.contentEquals(other.blockLight) &&
            skyLight.contentEquals(other.skyLight)

    override fun hashCode(): Int = 31 * blockLight.contentHashCode() + skyLight.contentHashCode()
}
