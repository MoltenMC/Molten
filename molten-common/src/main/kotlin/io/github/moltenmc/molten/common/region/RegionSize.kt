package io.github.moltenmc.molten.common.region

data class RegionSize(
    val chunksX: Int,
    val chunksZ: Int,
) {
    init {
        require(chunksX > 0) { "Region width must be positive." }
        require(chunksZ > 0) { "Region depth must be positive." }
    }

    companion object {
        val RECOMMENDED: RegionSize = RegionSize(8, 8)
    }
}
