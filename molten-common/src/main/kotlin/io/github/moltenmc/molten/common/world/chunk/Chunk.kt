package io.github.moltenmc.molten.common.world.chunk

import io.github.moltenmc.molten.common.nbt.NbtValue
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.section.ChunkSection

data class Chunk(
    val position: ChunkPos,
    val sections: List<ChunkSection>,
    val dataVersion: Int = 0,
    val status: String = "minecraft:empty",
    val inhabitedTime: Long = 0,
    val heightmaps: Map<String, LongArray> = emptyMap(),
    val blockEntities: List<BlockEntityData> = emptyList(),
    val rawData: Map<String, NbtValue> = emptyMap(),
    val dirtyMarkers: Set<DirtyMarker> = emptySet(),
) {
    override fun equals(other: Any?): Boolean =
        other is Chunk &&
            position == other.position &&
            sections == other.sections &&
            dataVersion == other.dataVersion &&
            status == other.status &&
            inhabitedTime == other.inhabitedTime &&
            heightmaps.keys == other.heightmaps.keys &&
            heightmaps.all { (key, value) -> value.contentEquals(other.heightmaps[key]) } &&
            blockEntities == other.blockEntities &&
            rawData == other.rawData &&
            dirtyMarkers == other.dirtyMarkers

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + sections.hashCode()
        result = 31 * result + dataVersion
        result = 31 * result + status.hashCode()
        result = 31 * result + inhabitedTime.hashCode()
        result = 31 * result + heightmaps.entries.fold(0) { acc, (key, value) ->
            acc + 31 * key.hashCode() + value.contentHashCode()
        }
        result = 31 * result + blockEntities.hashCode()
        result = 31 * result + rawData.hashCode()
        result = 31 * result + dirtyMarkers.hashCode()
        return result
    }
}
