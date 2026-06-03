package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.modLoc
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.foundation.data.AssetLookup
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.fluids.BaseFlowingFluid

object SDFluids {
    @JvmField
    val FERTILIZER =
        REGISTRATE
            .`object`("fertilizer")
            .fluid(
                modLoc("block/fluid/fertilizer_still"),
                modLoc("block/fluid/fertilizer_flowing"),
            ).lang("Liquid Fertilizer")
            .tag(SDTags.FERTILIZER_FLUIDS)
            .source { BaseFlowingFluid.Source(it) }
            .bucket()
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .model(AssetLookup.existingItemModel())
            .lang("Bucket of Liquid Fertilizer")
            .tag(Tags.Items.BUCKETS)
            .build()
            .register()
}
