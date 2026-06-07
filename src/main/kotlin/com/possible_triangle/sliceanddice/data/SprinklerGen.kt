package com.possible_triangle.sliceanddice.data

import com.possible_triangle.atmosphere.api.v1.WeatherCondition
import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.actions.DamageAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.ExperienceAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.FertilizerAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.PotionAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.WeatherAction
import com.possible_triangle.sliceanddice.index.SDTags
import com.possible_triangle.sliceanddice.modLoc
import com.simibubi.create.AllFluids
import com.tterrag.registrate.AbstractRegistrate
import galena.oreganized.index.ODamageSources
import galena.oreganized.index.OTags
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageTypes
import net.neoforged.neoforge.fluids.crafting.FluidIngredient
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids

internal fun AbstractRegistrate<*>.registerSprinklers() {
    dataGenInitializer.add(SDRegistries.SPRINKLERS) {
        val damageTypes = it.lookup(Registries.DAMAGE_TYPE)

        it.register("rain", FluidIngredient.tag(SDTags.WET_FLUIDS), WeatherAction(WeatherCondition.RAIN))
        it.register(
            "burn",
            FluidIngredient.tag(SDTags.HOT_FLUIDS),
            DamageAction(damageTypes.getOrThrow(DamageTypes.IN_FIRE), 0.5F),
        )
        it.register(
            "fertilize",
            FluidIngredient.tag(SDTags.FERTILIZER_FLUIDS),
            FertilizerAction,
        )
        it.register(
            "potion",
            FluidIngredient.of(AllFluids.POTION.get()),
            PotionAction,
        )
        it.register(
            "experience",
            FluidIngredient.of(CEIFluids.EXPERIENCE.get()),
            ExperienceAction(1F),
        )
        it.register(
            "lead",
            FluidIngredient.tag(OTags.Fluids.MOLTEN_LEAD),
            DamageAction(damageTypes.getOrThrow(ODamageSources.MOLTEN_LEAD), 0.5F),
        )
    }
}

internal fun <T : SprinkleAction> BootstrapContext<Sprinkler<*>>.register(
    name: String,
    fluid: FluidIngredient,
    config: T,
) {
    val key = ResourceKey.create(SDRegistries.SPRINKLERS, modLoc(name))
    register(key, Sprinkler(fluid, config = config))
}
