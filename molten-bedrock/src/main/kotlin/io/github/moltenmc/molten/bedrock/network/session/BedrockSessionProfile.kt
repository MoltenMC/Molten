package io.github.moltenmc.molten.bedrock.network.session

import java.util.UUID

data class BedrockSessionProfile(
    val uuid: UUID,
    val displayName: String,
    val deviceModel: String? = null,
)
