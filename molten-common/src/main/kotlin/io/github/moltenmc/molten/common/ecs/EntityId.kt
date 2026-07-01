package io.github.moltenmc.molten.common.ecs

@JvmInline
value class EntityId(val value: Long) {
    init {
        require(value >= 0) { "Entity id must be non-negative." }
    }

    val index: Int
        get() = (value and INDEX_MASK).toInt()

    val generation: Int
        get() = ((value ushr INDEX_BITS) and GENERATION_MASK).toInt()

    val kind: EntityKind
        get() = EntityKind.fromId(((value ushr KIND_SHIFT) and KIND_MASK).toInt())

    companion object {
        private const val INDEX_BITS = 32
        private const val GENERATION_BITS = 24
        private const val KIND_BITS = 8
        private const val KIND_SHIFT = INDEX_BITS + GENERATION_BITS
        private const val INDEX_MASK = 0xFFFF_FFFFL
        private const val GENERATION_MASK = 0xFF_FFFFL
        private const val KIND_MASK = 0xFFL
        private val INDEX_RANGE = 0..UInt.MAX_VALUE.toLong()
        private val GENERATION_RANGE = 0 until (1 shl GENERATION_BITS)
        private val KIND_RANGE = 0 until (1 shl KIND_BITS)

        fun of(index: Long, generation: Int, kind: EntityKind): EntityId {
            require(index in INDEX_RANGE) { "Entity index must fit in 32 bits." }
            require(generation in GENERATION_RANGE) { "Entity generation must fit in 24 bits." }
            require(kind.id in KIND_RANGE) { "Entity kind must fit in 8 bits." }

            return EntityId(
                (index and INDEX_MASK) or
                    ((generation.toLong() and GENERATION_MASK) shl INDEX_BITS) or
                    ((kind.id.toLong() and KIND_MASK) shl KIND_SHIFT),
            )
        }
    }
}
