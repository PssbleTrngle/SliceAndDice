package com.possible_triangle.sliceanddice

import com.possible_triangle.sliceanddice.SliceAndDice.MOD_ID
import com.possible_triangle.sliceanddice.block.slicer.SlicerTile
import com.possible_triangle.sliceanddice.compat.ModCompat
import com.simibubi.create.AllFluids
import com.simibubi.create.Create.RANDOM
import com.simibubi.create.content.fluids.FluidFX
import com.simibubi.create.content.fluids.potion.PotionFluid
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity
import com.simibubi.create.content.kinetics.press.PressingBehaviour
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper
import com.simibubi.create.foundation.ponder.SceneBuilder
import com.simibubi.create.foundation.ponder.element.InputWindowElement
import com.simibubi.create.foundation.utility.Pointing
import com.simibubi.create.foundation.utility.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FarmBlock
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

object PonderScenes {

    private val HELPER = PonderRegistrationHelper(MOD_ID)

    private fun SceneBuilder.intro(key: String, text: String, size: Int) {
        title(key, text)
        configureBasePlate(0, 0, size)
        showBasePlate()
        idle(5)
    }

    private fun SceneBuilder.sprinklerParticles(fluid: FluidStack, at: BlockPos, ticks: Int) {
        val particle = FluidFX.getFluidParticle(fluid)
        effects.emitParticles(
            VecHelper.getCenterOf(at),
            { w, x, y, z ->
                w.addParticle(particle, x, y, z, RANDOM.nextDouble(-0.1, 0.1), 0.0, RANDOM.nextDouble(-0.1, 0.1))
            },
            1F, ticks
        )
    }

    private fun SceneBuilder.fillTank(at: BlockPos, fluid: FluidStack) {
        world.modifyBlockEntity(at, FluidTankBlockEntity::class.java) { be ->
            be.tankInventory.apply {
                fluid.amount.takeIf { it > 0 }?.let {
                    drain(it, FluidAction.EXECUTE)
                    idle(10)
                }
                fill(fluid, FluidAction.EXECUTE)
            }
        }
    }

    fun register() {
        HELPER.forComponents(Content.SLICER_BLOCK).addStoryBoard("slicer") { scene, util ->
            scene.intro("slicer", "Cutting with the slicer", 5)

            val motor = util.select.fromTo(2, 1, 3, 2, 4, 3)
            val beltSlicer = util.grid.at(2, 3, 2)

            val basin = util.grid.at(1, 2, 3)
            val basinSlicer = util.grid.at(1, 4, 3)

            val bigCog = util.select.position(3, 0, 5)
            val belt = util.select.fromTo(1, 1, 2, 4, 1, 2).add(util.select.fromTo(4, 1, 3, 4, 1, 5)).add(bigCog)

            scene.world.setKineticSpeed(util.select.position(basinSlicer), -64F)
            scene.world.setKineticSpeed(util.select.position(beltSlicer), -64F)
            scene.world.setKineticSpeed(motor, 64F)
            scene.world.setKineticSpeed(belt, -8F)
            scene.world.setKineticSpeed(bigCog, 4F)

            val beltSlicerSection = scene.world.showIndependentSection(util.select.position(beltSlicer), Direction.UP)
            scene.effects.indicateSuccess(beltSlicer)
            scene.idle(5)

            scene.world.showSection(motor, Direction.UP)
            scene.idle(5)

            scene.world.showSection(belt, Direction.SOUTH)
            scene.idle(20)

            val knife = ItemStack(ModCompat.exampleTool)
            scene.overlay.showControls(
                InputWindowElement(
                    VecHelper.getCenterOf(beltSlicer.above()), Pointing.DOWN
                ).withItem(knife), 50
            )
            scene.world.modifyBlockEntity(beltSlicer, SlicerTile::class.java) {
                it.heldItem = knife
            }
            scene.world.modifyBlockEntity(basinSlicer, SlicerTile::class.java) {
                it.heldItem = knife
            }

            scene.overlay.showText(60).text("Right-click it with a valid tool").placeNearTarget()
                .pointAt(util.vector.blockSurface(beltSlicer, Direction.WEST))

            scene.idle(5)

            val beltOutputPos = beltSlicer.below(2)
            val beltInputPos = beltOutputPos.west()
            scene.world.createItemOnBeltLike(beltInputPos, Direction.UP, ItemStack(ModCompat.exampleInput))

            scene.idleSeconds(4)

            val slices = ItemStack(ModCompat.exampleOutput, 7)
            scene.world.removeItemsFromBelt(beltOutputPos)
            val slicesInWorld = scene.world.createItemOnBelt(beltOutputPos, Direction.UP, slices)

            scene.world.modifyBlockEntity(beltSlicer, SlicerTile::class.java) {
                it.cuttingBehaviour.makePressingParticleEffect(
                    VecHelper.getCenterOf(beltOutputPos).add(0.0, 0.6, 0.0), slices
                )
            }

            scene.idle(5)
            scene.world.stallBeltItem(slicesInWorld, false)
            scene.world.modifyBlockEntity(beltSlicer, SlicerTile::class.java) {
                //it.cuttingBehaviour.running = false
            }

            scene.idleSeconds(3)
            scene.addKeyframe()

            scene.world.showSection(util.select.fromTo(1, 1, 3, 1, 4, 3), Direction.EAST)
            scene.idle(5)
            scene.world.hideIndependentSection(beltSlicerSection, Direction.EAST)

            scene.overlay.showText(60).text("The slicer can also operate on a basin").placeNearTarget()
                .pointAt(util.vector.blockSurface(basinSlicer, Direction.WEST))

            scene.idle(5)
            scene.world.setBlock(beltSlicer, Blocks.AIR.defaultBlockState(), false)

            val basinOutputPos = basin.north().below()
            for (i in 0..3) {
                scene.idleSeconds(2)
                scene.world.modifyBlockEntity(basinSlicer, SlicerTile::class.java) {
                    it.cuttingBehaviour.start(PressingBehaviour.Mode.BASIN)
                }
                scene.idleSeconds(1)
                scene.world.createItemOnBeltLike(basinOutputPos, Direction.SOUTH, slices)
                scene.idleSeconds(1)
                scene.world.createItemOnBeltLike(basinOutputPos, Direction.SOUTH, slices)
            }
        }

        HELPER.forComponents(Content.SPRINKLER_BLOCK, Content.FERTILIZER_BUCKET).addStoryBoard("sprinkler/intro") { scene, util ->
            scene.intro("sprinkler/intro", "Sprinkles on top", 5)

            val tank = util.select.fromTo(4, 1, 2, 4, 4, 2)
            val pump = util.grid.at(3, 4, 2)
            val pipe = util.select.fromTo(3, 4, 2, 1, 4, 2)
            val motor = util.select.fromTo(4, 4, 3, 3, 4, 3)
            val sprinkler = util.grid.at(1, 3, 2)

            scene.world.showSection(tank, Direction.EAST)
            scene.idle(5)

            scene.world.showSection(pipe.add(util.select.position(pump)), Direction.DOWN)
            scene.idle(5)
            scene.world.showSection(motor, Direction.DOWN)
            scene.idle(20)

            scene.world.showSection(util.select.position(sprinkler), Direction.UP)
            scene.overlay.showText(60).text("Place a sprinkler below a pipe").placeNearTarget()
                .pointAt(util.vector.blockSurface(sprinkler, Direction.DOWN))

            scene.idle(5)

            val fluid = FluidStack(Fluids.WATER, 30000)
            scene.fillTank(util.grid.at(4, 1, 2), fluid)

            scene.world.setKineticSpeed(motor, 16F)
            scene.world.setKineticSpeed(util.select.position(pump), -16F)
            scene.world.propagatePipeChange(pump)
            scene.idle(5)

            scene.sprinklerParticles(fluid, sprinkler, 240)
            scene.idle(60)

            scene.addKeyframe()

            val farmland = util.select.fromTo(1, 1, 1, 2, 1, 3)
            val farmlandWithBorder = util.select.fromTo(0, 1, 0, 3, 1, 4)
            scene.world.showSection(farmlandWithBorder, Direction.UP)
            scene.idle(20)

            val shuffledBlocks = arrayListOf<BlockPos>()
            farmland.forEach { shuffledBlocks.add(BlockPos(it.x, it.y, it.z)) }

            shuffledBlocks.shuffled().forEach {
                scene.world.replaceBlocks(
                    util.select.position(it),
                    Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7),
                    false
                )
                scene.idle(20)
            }

        }.addStoryBoard("sprinkler/uses") { scene, util ->
            scene.intro("sprinkler/uses", "Types of sprinkles", 7)
            scene.idle(5)

            val tankController = util.grid.at(3, 1, 4)
            val tank = util.select.fromTo(3, 1, 4, 5, 5, 6)
            val sprinkler = util.grid.at(3, 4, 1)
            val pump = util.grid.at(3, 5, 3)
            val pipe = util.select.fromTo(sprinkler, pump)

            val shaft = util.select.fromTo(2, 4, 3, 2, 4, 7)
            val cog = util.select.position(3, 3, 7)

            scene.world.showSection(tank.add(pipe), Direction.DOWN)
            scene.idle(5)
            scene.world.showSection(shaft.add(cog), Direction.EAST)
            scene.idle(5)

            scene.world.setKineticSpeed(shaft, 16F)
            scene.world.setKineticSpeed(cog, -16F)
            scene.world.setKineticSpeed(util.select.position(pump), -16F)

            scene.idle(10)

            val cowPos = util.vector.topOf(sprinkler.below(3))
            val cow = scene.world.createEntity {
                EntityType.COW.create(it)!!.apply {
                    setPos(cowPos.x, cowPos.y, cowPos.z)
                    xo = cowPos.x
                    yo = cowPos.y
                    zo = cowPos.z
                    walkAnimation.update(-walkAnimation.position(), 1f)
                    walkAnimation.setSpeed(1f)
                    yRotO = 210f
                    yRot = 210f
                    yHeadRotO = 210f
                    yHeadRot = 210f
                }
            }

            scene.overlay.showText(60)
                .text("Different fluids also affect entities differently")
                .pointAt(cowPos)

            scene.idleSeconds(3)

            scene.addKeyframe()

            scene.overlay.showControls(
                InputWindowElement(
                    tank.center,
                    Pointing.LEFT
                ).withItem(ItemStack(Items.LAVA_BUCKET)), 10
            )

            FluidStack(Fluids.LAVA, 50000).also { fluid ->
                scene.fillTank(tankController, fluid)
                scene.world.propagatePipeChange(pump)
                scene.idle(4)
                scene.sprinklerParticles(fluid, sprinkler, 80)
            }

            scene.idle(10)

            repeat(4) {
                scene.world.modifyEntity(cow) {
                    it.animateHurt(1F)
                }
                scene.idleSeconds(1)
            }

            scene.addKeyframe()

            scene.overlay.showControls(
                InputWindowElement(
                    tank.center,
                    Pointing.LEFT
                ).withItem(ItemStack(Items.POTION).also {
                    PotionUtils.setPotion(it, Potions.INVISIBILITY)
                }), 10
            )

            FluidStack(AllFluids.POTION.get(), 100000, CompoundTag()).also { fluid ->
                PotionFluid.addPotionToFluidStack(fluid, Potions.INVISIBILITY)

                scene.fillTank(tankController, fluid)
                scene.world.propagatePipeChange(pump)
                scene.idle(4)
                scene.sprinklerParticles(fluid, sprinkler, 60)
            }

            scene.idle(10)

            scene.world.modifyEntity(cow) {
                it.isInvisible = true
            }

            val color = MobEffects.INVISIBILITY.color
            val r: Double = (color shr 16 and 255).toDouble() / 255.0
            val g: Double = (color shr 8 and 255).toDouble() / 255.0
            val b: Double = (color shr 0 and 255).toDouble() / 255.0
            scene.effects.emitParticles(
                cowPos.add(0.0, 1.0, 0.0),
                { w, x, y, z ->
                    w.addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        x + RANDOM.nextDouble(-0.4, 0.4),
                        y + RANDOM.nextDouble(-0.4, 0.4),
                        z + RANDOM.nextDouble(-0.4, 0.4),
                        r,
                        g,
                        b
                    )
                },
                1F, 240
            )

            scene.idleSeconds(2)
        }
    }

}