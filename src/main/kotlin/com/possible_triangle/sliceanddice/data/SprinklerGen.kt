package com.possible_triangle.sliceanddice.data

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.Content.FERTILIZER_FLUIDS
import com.possible_triangle.sliceanddice.Content.HOT_FLUIDS
import com.possible_triangle.sliceanddice.api.ModRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction
import com.simibubi.create.AllFluids
import com.tterrag.registrate.AbstractRegistrate
import net.minecraft.core.Holder
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.fluids.crafting.FluidIngredient

internal fun AbstractRegistrate<*>.registerSprinklers() {
    dataGenInitializer.add(ModRegistries.SPRINKLERS) {
        it.register(FluidIngredient.tag(Content.WET_FLUIDS), Content.MOIST_ACTION)
        it.register(FluidIngredient.tag(HOT_FLUIDS), Content.BURNING_ACTION)
        it.register(FluidIngredient.tag(FERTILIZER_FLUIDS), Content.FERTILIZER_ACTION)
        it.register(FluidIngredient.of(AllFluids.POTION.get()), Content.POTION_ACTION)
    }
}

internal fun BootstrapContext<Sprinkler>.register(
    fluid: FluidIngredient,
    action: Holder<SprinkleAction>,
) {
    val key = ResourceKey.create(ModRegistries.SPRINKLERS, action.key!!.location())
    register(key, Sprinkler(fluid, action))
}
