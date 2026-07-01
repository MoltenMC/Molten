package io.github.moltenmc.molten.java.login

import io.github.moltenmc.molten.java.network.session.JavaSession

interface JavaLoginHandler {
    fun accept(session: JavaSession)
}
