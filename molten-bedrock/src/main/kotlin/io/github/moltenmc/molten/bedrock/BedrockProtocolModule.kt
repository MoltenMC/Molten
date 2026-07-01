package io.github.moltenmc.molten.bedrock

import io.github.moltenmc.molten.common.runtime.RuntimeModule
import io.github.moltenmc.molten.bedrock.world.leveldb.BedrockSubChunkDecoder
import io.github.moltenmc.molten.bedrock.world.leveldb.RawPreservingLevelDbChunkMapper
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeDataSource
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeIdTranslator
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeRegistryBootstrap

class BedrockProtocolModule(
    private val blockRuntimeDataSources: Collection<BedrockBlockRuntimeDataSource> =
        listOf(BedrockBlockRuntimeDataSource.vanillaDefaults()),
) : RuntimeModule {
    override val name: String = "molten-bedrock"

    var blockRuntimeIdTranslator: BedrockBlockRuntimeIdTranslator =
        BedrockBlockRuntimeRegistryBootstrap(blockRuntimeDataSources).bootstrap()
        private set

    override fun start() {
        blockRuntimeIdTranslator = BedrockBlockRuntimeRegistryBootstrap(blockRuntimeDataSources).bootstrap()
    }

    override fun stop() {
    }

    fun createLevelDbChunkMapper(): RawPreservingLevelDbChunkMapper =
        RawPreservingLevelDbChunkMapper(
            subChunkDecoder = BedrockSubChunkDecoder(blockRuntimeIdTranslator),
        )
}
