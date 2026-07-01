package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.leveldb.LevelDB
import io.github.moltenmc.molten.common.world.ChunkPos
import io.github.moltenmc.molten.common.world.chunk.Chunk
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

class DefaultLevelDbStorageAdapter(
    databasePath: Path,
    private val mapper: LevelDbChunkMapper = RawPreservingLevelDbChunkMapper(),
    private val dimension: Int = BedrockChunkRecordKey.OVERWORLD_DIMENSION,
    private val ioExecutor: Executor = ForkJoinPool.commonPool(),
) : LevelDbStorageAdapter {
    private val database = LevelDB(databasePath)

    override fun loadChunk(position: ChunkPos): CompletableFuture<Chunk?> =
        CompletableFuture.supplyAsync(
            {
                val versionKey = BedrockChunkRecordKey.version(position, dimension).encode()
                if (database.get(versionKey) == null) {
                    return@supplyAsync null
                }

                val records = collectChunkRecords(position)
                mapper.toChunk(position, records)
            },
            ioExecutor,
        )

    override fun saveChunk(chunk: Chunk): CompletableFuture<Void> =
        CompletableFuture.runAsync(
            {
                mapper.toRecords(chunk).forEach { (key, value) -> database.put(key, value) }
                database.flush()
            },
            ioExecutor,
        )

    override fun close() {
        database.close()
    }

    private fun collectChunkRecords(position: ChunkPos): Map<ByteArray, ByteArray> {
        val records = LinkedHashMap<ByteArray, ByteArray>()
        database.iterator().let { iterator ->
            iterator.seek(chunkKeyPrefix(position))
            while (iterator.isValid()) {
                val key = iterator.getKey()
                if (!key.startsWithChunkPrefix(position)) {
                    break
                }
                records[key] = iterator.getValue()
                iterator.next()
            }
        }
        return records
    }

    private fun chunkKeyPrefix(position: ChunkPos): ByteArray =
        BedrockChunkRecordKey(position, dimension, tag = 0).encode().copyOf(
            BedrockChunkRecordKey.chunkPrefixLength(dimension),
        )

    private fun ByteArray.startsWithChunkPrefix(position: ChunkPos): Boolean {
        val prefix = chunkKeyPrefix(position)
        return size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
    }
}
