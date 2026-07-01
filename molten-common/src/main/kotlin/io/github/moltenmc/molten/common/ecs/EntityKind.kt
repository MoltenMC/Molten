package io.github.moltenmc.molten.common.ecs

enum class EntityKind(val id: Int) {
    GENERIC(0),
    PLAYER(1),
    MOB(2),
    ITEM_ENTITY(3),
    PROJECTILE(4),
    VEHICLE(5),
    BLOCK_ENTITY(6),
    EXPERIENCE_ORB(7),
    FALLING_BLOCK(8),
    ;

    companion object {
        private val byId = entries.associateBy(EntityKind::id)

        fun fromId(id: Int): EntityKind = byId[id] ?: GENERIC
    }
}
