package io.github.moltenmc.molten.common.performance

import java.time.Instant

data class MetricSample(
    val metric: PerformanceMetric,
    val value: Double,
    val capturedAt: Instant = Instant.now(),
)
