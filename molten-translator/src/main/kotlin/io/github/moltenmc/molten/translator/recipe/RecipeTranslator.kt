package io.github.moltenmc.molten.translator.recipe

interface RecipeTranslator {
    fun translate(recipe: Any): Any
}
