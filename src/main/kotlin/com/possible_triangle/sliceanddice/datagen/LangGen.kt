package com.possible_triangle.sliceanddice.datagen

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.possible_triangle.sliceanddice.PonderScenes
import com.possible_triangle.sliceanddice.SliceAndDice
import com.simibubi.create.foundation.data.LangPartial
import com.simibubi.create.foundation.ponder.PonderLocalization
import com.simibubi.create.infrastructure.ponder.AllPonderTags
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataGenerator
import net.minecraft.data.DataProvider

private typealias LangProvider = () -> JsonObject

class LangGen(private val generator: DataGenerator) : DataProvider {

    private val GSON = GsonBuilder().setPrettyPrinting().create()

    companion object {
        private val PARTIALS = listOf<LangProvider>(
            fromResource("partials"),
            ::ponderPartials
        )

        private fun fromResource(fileName: String): LangProvider = {
            LangPartial.fromResource(SliceAndDice.MOD_ID, fileName) as JsonObject
        }

        private fun ponderPartials(): JsonObject {
            AllPonderTags.register()
            PonderScenes.register()

            PonderLocalization.generateSceneLang()

            return JsonObject().also {
                PonderLocalization.record(SliceAndDice.MOD_ID, it)
            }
        }
    }

    override fun run(cache: CachedOutput) {
        val json = JsonObject()
        PARTIALS.map { it() }.forEach { partial ->
            partial.entrySet().forEach { json.add(it.key, it.value) }
        }

        val target = generator.outputFolder.resolve("assets/${SliceAndDice.MOD_ID}/lang/en_us.json")
        DataProvider.saveStable(cache, json, target)
    }

    override fun getName() = "${SliceAndDice.MOD_ID} lang partials"

}