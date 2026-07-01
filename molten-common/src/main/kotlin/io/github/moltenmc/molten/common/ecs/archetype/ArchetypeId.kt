package io.github.moltenmc.molten.common.ecs.archetype

@JvmInline
value class ArchetypeId(val value: Int) {
    init {
        require(value >= 0) { "Archetype id must be non-negative." }
    }
}
