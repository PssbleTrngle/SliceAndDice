package com.possible_triangle.sliceanddice.datagen

import com.google.gson.JsonObject
import com.possible_triangle.sliceanddice.PonderScenes
import com.possible_triangle.sliceanddice.SliceAndDice
import com.simibubi.create.foundation.data.LangPartial
import com.simibubi.create.foundation.ponder.PonderLocalization
import com.simibubi.create.infrastructure.ponder.AllPonderTags
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

private typealias LangProvider = () -> JsonObject

class LangGen(output: PackOutput) : DataProvider {

    private val pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang")

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

    override fun run(cache: CachedOutput): CompletableFuture<*> {
        val json = JsonObject()
        PARTIALS.map { it() }.forEach { partial ->
            partial.entrySet().forEach { json.add(it.key, it.value) }
        }

        val target = pathProvider.json(ResourceLocation(SliceAndDice.MOD_ID, "en_us"))
        return DataProvider.saveStable(cache, json, target)
    }

    override fun getName() = "${SliceAndDice.MOD_ID} lang partials"

}