package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkeContext
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.data.register
import com.tterrag.registrate.AbstractRegistrate
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.fluids.crafting.FluidIngredient
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids

class CreateEnchantmentIndustryCompat private constructor() : SprinkleAction {
    companion object {
        private val INSTANCE = CreateEnchantmentIndustryCompat()

        fun ifLoaded(runnable: CreateEnchantmentIndustryCompat.() -> Unit) {
            ModCompat.ifLoaded(ModCompat.CREATE_ENCHANTMENT_INDUSTRY) {
                runnable(INSTANCE)
            }
        }
    }

    override fun tick(context: SprinkeContext) {
        // The following code is evil!!!
        // This is an implementation detail of SprinkleTile.kt
        // Everytime some fluid is used by the sprinkler, the processingTicks value is set to 20
        // But the act function is only called when processingTicks is >= 8
        // This effectively means this function executes for 13 consecutive ticks
        // and then doesn't for 9 ticks
        // We keep this into account to calculate the amount of fluid used each update
        val fluidUsed = Configs.SERVER.sprinklerUsage.get()
        val actingTicks = 13.toFloat()
        val players = context.getEntities(Player::class.java)

        val totalAmount = ExperienceHelper.getExperienceFromFluid(context.fluidStack.copyWithAmount(fluidUsed))

        if (players.isEmpty()) {
            val blocks = ArrayList<BlockPos>()
            context.forEachGroundBlock { pos ->
                if (context.random.nextFloat() <= 0.25) {
                    blocks.add(pos)
                }
            }
            val xp = (totalAmount / actingTicks) / blocks.size.toFloat()
            blocks.forEach { pos ->
                val drop = ExperienceOrb(context.level, pos.center.x(), pos.center.y(), pos.center.z(), xp.toInt())
                drop.setPos(pos.center)
                context.level.addFreshEntity(drop)
            }
        } else {
            val xp = (totalAmount / actingTicks) / players.size.toFloat()
            players.forEach { player ->
                ExperienceOrb.award(context.level, player.position(), xp.toInt())
            }
        }
    }

    fun AbstractRegistrate<*>.registerSprinkleBehaviour() {
        val action =
            generic("experience", SDRegistries.SPRINKLER_ACTIONS) { INSTANCE }
                .register()

        dataGenInitializer.add(SDRegistries.SPRINKLERS) {
            it.register(
                FluidIngredient.of(CEIFluids.EXPERIENCE.get()),
                action,
            )
        }
    }
}
