package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.item.FloorSprinklerItem
import com.tterrag.registrate.providers.ProviderType
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless

object SDItems {
    val FERTILIZER_BUCKET = REGISTRATE.get("fertilizer_bucket", Registries.ITEM)

    val FLOOR_SPRINKLER =
        REGISTRATE
            .`object`("floor_sprinkler")
            .item(::FloorSprinklerItem)
            .setData(ProviderType.LANG) { _, _ -> }
            .model { c, p -> p.withExistingParent(c.name, c.id.withPrefix("block/")) }
            .recipe { c, p ->
                shapeless(RecipeCategory.MISC, c.entry)
                    .requires(c.entry.block)
                    .unlockedByPipe()
                    .save(p, "sprinkler_conversion_1")
            }.register()
}
