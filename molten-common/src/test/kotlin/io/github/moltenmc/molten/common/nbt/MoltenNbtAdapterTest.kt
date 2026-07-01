package io.github.moltenmc.molten.common.nbt

import io.github.moltenmc.nbt.NbtByte
import io.github.moltenmc.nbt.NbtCompound
import io.github.moltenmc.nbt.NbtInt
import io.github.moltenmc.nbt.NbtList
import io.github.moltenmc.nbt.NbtString
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MoltenNbtAdapterTest {
    @Test
    fun convertsCompoundFromLibraryTags() {
        val compound = NbtCompound().apply {
            putString("name", "Molten")
            putInt("protocol", 776)
            set("enabled", NbtByte(1))
        }

        val value = MoltenNbtAdapter.fromLibraryCompound(compound)

        assertEquals(NbtValue.StringValue("Molten"), value.values["name"])
        assertEquals(NbtValue.IntValue(776), value.values["protocol"])
        assertEquals(NbtValue.ByteValue(1), value.values["enabled"])
    }

    @Test
    fun convertsCompoundToLibraryTags() {
        val value = NbtValue.CompoundValue(
            mapOf(
                "name" to NbtValue.StringValue("Molten"),
                "protocol" to NbtValue.IntValue(1001),
                "bytes" to NbtValue.ByteArrayValue(byteArrayOf(1, 2, 3)),
            ),
        )

        val compound = MoltenNbtAdapter.toLibraryCompound(value)

        assertEquals("Molten", compound.getString("name"))
        assertEquals(1001, compound.getInt("protocol"))
        assertContentEquals(byteArrayOf(1, 2, 3), compound.getByteArray("bytes"))
    }

    @Test
    fun convertsLists() {
        val list = NbtList(NBT_INT).apply {
            add(NbtInt(1))
            add(NbtInt(2))
        }
        val compound = NbtCompound().apply {
            set("values", list)
        }

        val converted = MoltenNbtAdapter.fromLibraryCompound(compound)
        val values = assertIs<NbtValue.ListValue>(converted.values["values"])

        assertEquals(listOf(NbtValue.IntValue(1), NbtValue.IntValue(2)), values.values)
    }

    @Test
    fun writesListsBackToLibraryTags() {
        val value = NbtValue.ListValue(
            listOf(
                NbtValue.StringValue("java"),
                NbtValue.StringValue("bedrock"),
            ),
        )

        val list = assertIs<NbtList>(MoltenNbtAdapter.toLibrary(value))

        assertEquals(NBT_STRING, list.elementType)
        assertEquals(NbtString("java"), list.get(0))
        assertEquals(NbtString("bedrock"), list.get(1))
    }

    private companion object {
        const val NBT_INT: Byte = 3
        const val NBT_STRING: Byte = 8
    }
}
