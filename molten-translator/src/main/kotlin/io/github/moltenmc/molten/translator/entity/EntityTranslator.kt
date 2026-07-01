package io.github.moltenmc.molten.translator.entity

import io.github.moltenmc.molten.common.ecs.EntityKind

interface EntityTranslator {
    fun toExternalKind(kind: EntityKind): EntityKind
}
