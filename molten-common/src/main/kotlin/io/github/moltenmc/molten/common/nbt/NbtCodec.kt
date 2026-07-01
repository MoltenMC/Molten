package io.github.moltenmc.molten.common.nbt

import io.github.moltenmc.nbt.Nbt

object NbtCodec {
    fun decode(bytes: ByteArray, format: NbtFormat): NbtValue.CompoundValue {
        val compound = when (format) {
            NbtFormat.JAVA -> Nbt.readJava(bytes)
            NbtFormat.BEDROCK -> Nbt.readBedrock(bytes)
        }
        return MoltenNbtAdapter.fromLibraryCompound(compound)
    }

    fun encode(value: NbtValue.CompoundValue, format: NbtFormat, rootName: String = ""): ByteArray {
        val compound = MoltenNbtAdapter.toLibraryCompound(value)
        return when (format) {
            NbtFormat.JAVA -> Nbt.writeJava(compound, rootName)
            NbtFormat.BEDROCK -> Nbt.writeBedrock(compound, rootName)
        }
    }
}
