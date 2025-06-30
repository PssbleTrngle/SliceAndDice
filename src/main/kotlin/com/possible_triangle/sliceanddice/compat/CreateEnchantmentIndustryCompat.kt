package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import com.possible_triangle.sliceanddice.config.Configs
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.minecraftforge.fluids.FluidStack
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid
import plus.dragons.createenchantmentindustry.entry.CeiFluids
import kotlin.math.roundToInt

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
        val experienceFluid = fluidStack.fluid as ExperienceFluid

        if (players.isEmpty()) {
            val blocks = ArrayList<BlockPos>()
            range.forEachGroundBlock { pos ->
                if(random.nextFloat() <= 0.25) {
                    blocks.add(pos);
                }
            }
            val fluidToDrop = (fluidUsed.toFloat() / actingTicks) / blocks.size.toFloat()
            blocks.forEach { pos ->
                experienceFluid.drop(world, pos.center, fluidToDrop.roundToInt())
            }
        } else {
            val fluidPerPlayer = (fluidUsed.toFloat() / actingTicks) / players.size.toFloat()
            players.forEach { player ->
                experienceFluid.awardOrDrop(player, world, player.position(), Vec3.ZERO, fluidPerPlayer.roundToInt())
            }
        }

    }

    fun registerSprinkleBehaviour() {
        SprinkleBehaviour.register({
            it.fluid.fluidType == CeiFluids.EXPERIENCE.type ||
            it.fluid.fluidType == CeiFluids.HYPER_EXPERIENCE.type
       }, INSTANCE)
    }

}