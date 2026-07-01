package io.github.moltenmc.molten.bedrock.login

import io.github.moltenmc.molten.bedrock.network.session.BedrockSession

interface BedrockLoginHandler {
    fun accept(session: BedrockSession)
}
