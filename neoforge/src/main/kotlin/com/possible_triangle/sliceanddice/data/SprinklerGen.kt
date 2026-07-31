package com.possible_triangle.sliceanddice.data

import com.possible_triangle.atmosphere.api.v1.WeatherCondition
import com.possible_triangle.sliceanddice.MOD_ID
import com.possible_triangle.sliceanddice.api.data.SprinklerProvider
import com.possible_triangle.sliceanddice.block.sprinkler.actions.DamageAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.ExperienceAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.FertilizerAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.PotionAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.WeatherAction
import com.possible_triangle.sliceanddice.compat.ModCompat
import com.possible_triangle.sliceanddice.index.SDTags
import com.simibubi.create.AllFluids
import galena.oreganized.index.ODamageSources
import galena.oreganized.index.OTags
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageTypes
import net.neoforged.neoforge.common.conditions.ModLoadedCondition
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.fluids.crafting.FluidIngredient
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids

internal fun GatherDataEvent.registerSprinklers() {
    generator.addProvider(
        true,
        SprinklerProvider.of(
            generator.packOutput,
            lookupProvider,
            MOD_ID,
        ) {
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
                FluidIngredient.of(AllFluids.POTION.get().source),
                PotionAction,
            )
            it.register(
                "experience",
                FluidIngredient.of(CEIFluids.EXPERIENCE.get()),
                ExperienceAction(1F),
                listOf(ModLoadedCondition(ModCompat.CREATE_ENCHANTMENT_INDUSTRY)),
            )

            it.register(
                "lead",
                FluidIngredient.tag(OTags.Fluids.MOLTEN_LEAD),
                DamageAction(damageTypes.getOrThrow(ODamageSources.MOLTEN_LEAD), 0.5F),
                listOf(
                    ModLoadedCondition(
                        ModCompat.OREGANIZED,
                    ),
                ),
            )
        },
    )
}
