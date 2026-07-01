package io.github.moltenmc.molten.bedrock.world.leveldb

import io.github.moltenmc.molten.common.nbt.NbtValue

object BedrockChunkRawPayload {
    const val RAW_DATA_KEY = "bedrock:leveldb_records"
    const val RECORD_KEY = "key"
    const val RECORD_VALUE = "value"

    fun encode(records: Map<ByteArray, ByteArray>): NbtValue.ListValue =
        NbtValue.ListValue(
            records.map { (key, value) ->
                NbtValue.CompoundValue(
                    mapOf(
                        RECORD_KEY to NbtValue.ByteArrayValue(key),
                        RECORD_VALUE to NbtValue.ByteArrayValue(value),
                    ),
                )
            },
        )

    fun decode(value: NbtValue?): Map<ByteArray, ByteArray> =
        ((value as? NbtValue.ListValue)?.values).orEmpty()
            .filterIsInstance<NbtValue.CompoundValue>()
            .mapNotNull { compound ->
                val key = (compound.values[RECORD_KEY] as? NbtValue.ByteArrayValue)?.value
                val recordValue = (compound.values[RECORD_VALUE] as? NbtValue.ByteArrayValue)?.value
                if (key == null || recordValue == null) {
                    null
                } else {
                    key to recordValue
                }
            }
            .toMap()
}
