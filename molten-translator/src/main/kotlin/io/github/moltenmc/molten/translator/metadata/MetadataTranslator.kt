package io.github.moltenmc.molten.translator.metadata

interface MetadataTranslator {
    fun translate(metadata: Map<String, Any>): Map<String, Any>
}
