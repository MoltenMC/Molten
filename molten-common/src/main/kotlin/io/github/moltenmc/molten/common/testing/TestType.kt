package io.github.moltenmc.molten.common.testing

enum class TestType {
    UNIT,
    INTEGRATION,
    END_TO_END,
    PROPERTY_BASED,
    CONCURRENCY,
    STRESS,
    SOAK,
    FUZZ,
    BENCHMARK,
    COMPATIBILITY,
    MEMORY_LEAK,
    PLUGIN,
}
