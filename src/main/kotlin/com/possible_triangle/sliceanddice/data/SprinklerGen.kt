package com.possible_triangle.sliceanddice.data

import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.index.SDSprinklerActions
import com.possible_triangle.sliceanddice.index.SDTags
import com.simibubi.create.AllFluids
import com.tterrag.registrate.AbstractRegistrate
import net.minecraft.core.Holder
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.fluids.crafting.FluidIngredient

internal fun AbstractRegistrate<*>.registerSprinklers() {
    dataGenInitializer.add(SDRegistries.SPRINKLERS) {
        it.register(FluidIngredient.tag(SDTags.WET_FLUIDS), SDSprinklerActions.MOIST_ACTION)
        it.register(FluidIngredient.tag(SDTags.HOT_FLUIDS), SDSprinklerActions.BURNING_ACTION)
        it.register(
            FluidIngredient.tag(SDTags.FERTILIZER_FLUIDS),
            SDSprinklerActions.FERTILIZER_ACTION,
        ) { copy(tickRate = 20) }
        it.register(FluidIngredient.of(AllFluids.POTION.get()), SDSprinklerActions.POTION_ACTION)
    }
}

internal fun BootstrapContext<Sprinkler>.register(
    fluid: FluidIngredient,
    action: Holder<SprinkleAction>,
    modifier: Sprinkler.() -> Sprinkler = { this },
) {
    val key = ResourceKey.create(SDRegistries.SPRINKLERS, action.key!!.location())
    register(key, Sprinkler(fluid, action).modifier())
}
