package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.item.FloorSprinklerItem
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.foundation.data.AssetLookup
import com.tterrag.registrate.providers.ProviderType
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless

object SDItems {
    @JvmField
    val FERTILIZER_BUCKET = REGISTRATE.get("fertilizer_bucket", Registries.ITEM)

    @JvmField
    val FLOOR_SPRINKLER =
        REGISTRATE
            .`object`("floor_sprinkler")
            .item(::FloorSprinklerItem)
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .setData(ProviderType.LANG) { _, _ -> }
            .model(AssetLookup.customBlockItemModel("sprinkler", "floor", "item"))
            .recipe { c, p ->
                shapeless(RecipeCategory.MISC, c.entry)
                    .requires(c.entry.block)
                    .unlockedByPipe()
                    .save(p, "sprinkler_conversion_1")
            }.register()
}
