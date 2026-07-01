package io.github.moltenmc.molten.common.scheduler

enum class WorkerPoolKind(val blockingAllowed: BlockingPolicy) {
    NETWORK_IO(BlockingPolicy.FORBIDDEN),
    SIMULATION(BlockingPolicy.FORBIDDEN),
    WORLD_IO(BlockingPolicy.ALLOWED),
    ASYNC_PLUGIN(BlockingPolicy.LIMITED),
    BACKGROUND(BlockingPolicy.ALLOWED),
}
