package io.github.moltenmc.molten.api.service

import kotlin.reflect.KClass

interface ServiceRegistry {
    fun <T : Any> register(type: KClass<T>, service: T)

    fun <T : Any> find(type: KClass<T>): T?
}
