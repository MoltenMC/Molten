package io.github.moltenmc.molten.common.registry

enum class FallbackPolicy {
    CLOSEST_SAFE_EQUIVALENT,
    PLACEHOLDER_OR_DENY,
    HIDE_OR_REPLACE,
    EMULATE_OR_DISABLE,
}
