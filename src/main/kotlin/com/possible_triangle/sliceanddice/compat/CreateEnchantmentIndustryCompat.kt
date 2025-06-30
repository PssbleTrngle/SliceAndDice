package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import com.possible_triangle.sliceanddice.config.Configs
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.fluids.FluidStack
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids

class CreateEnchantmentIndustryCompat private constructor() : SprinkleBehaviour {
    companion object {
        private val INSTANCE = CreateEnchantmentIndustryCompat()

        fun ifLoaded(runnable: CreateEnchantmentIndustryCompat.() -> Unit) {
            ModCompat.ifLoaded(ModCompat.CREATE_ENCHANTMENT_INDUSTRY) {
                runnable(INSTANCE)
            }
        }
    }

    override fun act(
        range: SprinkleBehaviour.Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource
    ) {
        // The following code is evil!!!
        // This is an implementation detail of SprinkleTile.kt
        // Everytime some fluid is used by the sprinkler, the processingTicks value is set to 20
        // But the act function is only called when processingTicks is >= 8
        // This effectively means this function executes for 13 consecutive ticks
        // and then doesn't for 9 ticks
        // We keep this into account to calculate the amount of fluid used each update
        val fluidUsed = Configs.SERVER.SPRINKLER_USAGE.get()
        val actingTicks = 13.toFloat()
        val players = range.getEntities(Player::class.java)

        val totalAmount = ExperienceHelper.getExperienceFromFluid(fluidStack.copyWithAmount(fluidUsed))

        if (players.isEmpty()) {
            val blocks = ArrayList<BlockPos>()
            range.forEachGroundBlock { pos ->
                if(random.nextFloat() <= 0.25) {
                    blocks.add(pos);
                }
            }
            val xp = (totalAmount / actingTicks) / blocks.size.toFloat()
            blocks.forEach { pos ->
                val drop = ExperienceOrb(world, pos.center.x(), pos.center.y(), pos.center.z(), xp.toInt())
                drop.setPos(pos.center)
                world.addFreshEntity(drop)
            }
        } else {
            val xp = (totalAmount / actingTicks) / players.size.toFloat()
            players.forEach { player ->
                ExperienceOrb.award(world, player.position(), xp.toInt())
            }
        }

    }

    fun registerSprinkleBehaviour() {
        SprinkleBehaviour.register({
            it.fluid.fluidType == CEIFluids.EXPERIENCE.type
       }, INSTANCE)
    }

}