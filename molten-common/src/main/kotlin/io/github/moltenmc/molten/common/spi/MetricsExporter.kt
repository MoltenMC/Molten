package io.github.moltenmc.molten.common.spi

interface MetricsExporter {
    fun export(metrics: Map<String, Double>)
}
