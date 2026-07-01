package io.github.moltenmc.molten.bedrock

import io.github.moltenmc.molten.common.runtime.RuntimeModule
import io.github.moltenmc.molten.bedrock.world.leveldb.BedrockData2DDecoder
import io.github.moltenmc.molten.bedrock.world.leveldb.BedrockSubChunkDecoder
import io.github.moltenmc.molten.bedrock.world.leveldb.RawPreservingLevelDbChunkMapper
import io.github.moltenmc.molten.translator.biome.BedrockBiomeDataSource
import io.github.moltenmc.molten.translator.biome.BedrockBiomeIdTranslator
import io.github.moltenmc.molten.translator.biome.BedrockBiomeRegistryBootstrap
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeDataSource
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeIdTranslator
import io.github.moltenmc.molten.translator.block.BedrockBlockRuntimeRegistryBootstrap

class BedrockProtocolModule(
    private val blockRuntimeDataSources: Collection<BedrockBlockRuntimeDataSource> =
        listOf(BedrockBlockRuntimeDataSource.vanillaDefaults()),
    private val biomeDataSources: Collection<BedrockBiomeDataSource> =
        listOf(BedrockBiomeDataSource.vanillaDefaults()),
) : RuntimeModule {
    override val name: String = "molten-bedrock"

    var blockRuntimeIdTranslator: BedrockBlockRuntimeIdTranslator =
        BedrockBlockRuntimeRegistryBootstrap(blockRuntimeDataSources).bootstrap()
        private set

    var biomeIdTranslator: BedrockBiomeIdTranslator =
        BedrockBiomeRegistryBootstrap(biomeDataSources).bootstrap()
        private set

    override fun start() {
        blockRuntimeIdTranslator = BedrockBlockRuntimeRegistryBootstrap(blockRuntimeDataSources).bootstrap()
        biomeIdTranslator = BedrockBiomeRegistryBootstrap(biomeDataSources).bootstrap()
    }

    override fun stop() {
    }

    fun createLevelDbChunkMapper(): RawPreservingLevelDbChunkMapper =
        RawPreservingLevelDbChunkMapper(
            subChunkDecoder = BedrockSubChunkDecoder(blockRuntimeIdTranslator),
            data2DDecoder = BedrockData2DDecoder(biomeIdTranslator),
        )
}
