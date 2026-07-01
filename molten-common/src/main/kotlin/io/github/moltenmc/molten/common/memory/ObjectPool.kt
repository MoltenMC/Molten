package io.github.moltenmc.molten.common.memory

interface ObjectPool<T> {
    fun acquire(): T

    fun release(value: T)
}
