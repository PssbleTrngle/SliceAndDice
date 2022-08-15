package com.possible_triangle.thermomix

import com.possible_triangle.thermomix.ThermomixMod.MOD_ID
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper
import com.simibubi.create.foundation.ponder.element.InputWindowElement
import com.simibubi.create.foundation.utility.Pointing
import com.simibubi.create.foundation.utility.VecHelper
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import vectorwing.farmersdelight.common.registry.ModItems

object PonderScenes {

    private val HELPER = PonderRegistrationHelper(MOD_ID)

    fun register() {
        HELPER.forComponents(Content.THERMOMIX_BLOCK).addStoryBoard("thermomix") { scene, util ->
            scene.title(Content.THERMOMIX_BLOCK.id.path, "Cutting with the thermomix")
            scene.configureBasePlate(0, 0, 5)
            scene.showBasePlate()
            scene.idle(5)

            val motor = util.select.fromTo(2, 1, 3, 2, 4, 3)
            val beltThermomix = util.grid.at(2, 3, 2)

            val basin = util.grid.at(1, 2, 3)
            val basinThermomix = util.grid.at(1, 4, 3)

            val bigCog = util.select.position(3, 0, 5)
            val belt = util.select.fromTo(1, 1, 2, 4, 1, 2)
                .add(util.select.fromTo(4, 1, 3, 4, 1, 5))
                .add(bigCog)

            scene.world.setKineticSpeed(util.select.position(basinThermomix), -64F)
            scene.world.setKineticSpeed(util.select.position(beltThermomix), -64F)
            scene.world.setKineticSpeed(motor, 64F)
            scene.world.setKineticSpeed(belt, -8F)
            scene.world.setKineticSpeed(bigCog, 4F)

            val beltThermomixSection =
                scene.world.showIndependentSection(util.select.position(beltThermomix), Direction.UP)
            scene.effects.indicateSuccess(beltThermomix)
            scene.idle(5)

            scene.world.showSection(motor, Direction.UP)
            scene.idle(5)

            scene.world.showSection(belt, Direction.SOUTH)
            scene.idle(20)

            val knife = ItemStack(ModItems.IRON_KNIFE.get())
            scene.overlay.showControls(
                InputWindowElement(
                    VecHelper.getCenterOf(beltThermomix.above()),
                    Pointing.DOWN
                ).withItem(knife), 50
            )
            scene.world.modifyTileEntity(beltThermomix, ThermomixTile::class.java) {
                it.heldItem = knife
            }

            scene.overlay.showText(60)
                .text("Right-click it with a valid tool")
                .placeNearTarget()
                .pointAt(util.vector.blockSurface(beltThermomix, Direction.WEST))

            scene.idle(5)

            val beltOutputPos = beltThermomix.below(2)
            val beltInputPos = beltOutputPos.west()
            scene.world.createItemOnBeltLike(beltInputPos, Direction.UP, ItemStack(Items.CAKE))

            scene.idleSeconds(4)

            val slices = ItemStack(ModItems.CAKE_SLICE.get(), 7)
            scene.world.removeItemsFromBelt(beltOutputPos)
            val slicesInWorld = scene.world.createItemOnBelt(beltOutputPos, Direction.UP, slices)

            scene.world.modifyTileEntity(beltThermomix, ThermomixTile::class.java) {
                it.cuttingBehaviour.makePressingParticleEffect(
                    VecHelper.getCenterOf(beltOutputPos).add(0.0, 0.6, 0.0),
                    slices
                )
            }

            scene.idle(5)
            scene.world.stallBeltItem(slicesInWorld, false)
            scene.world.modifyTileEntity(beltThermomix, ThermomixTile::class.java) {
                //it.cuttingBehaviour.running = false
            }

            scene.idleSeconds(3)
            scene.addKeyframe()

            scene.world.showSection(util.select.fromTo(1, 1, 3, 1, 4, 3), Direction.EAST)
            scene.idle(5)
            scene.world.hideIndependentSection(beltThermomixSection, Direction.EAST)

            scene.overlay.showText(60)
                .text("The Thermomix can also operate on a basin")
                .placeNearTarget()
                .pointAt(util.vector.blockSurface(basinThermomix, Direction.WEST))

            scene.idle(5)
            scene.world.setBlock(beltThermomix, Blocks.AIR.defaultBlockState(), false)

            val basinOutputPos = basin.north().below()
            for (i in 0..3) {
                scene.idleSeconds(2)
                scene.world.modifyTileEntity(basinThermomix, ThermomixTile::class.java) {
                    it.cuttingBehaviour.start(PressingBehaviour.Mode.BASIN)
                }
                scene.idleSeconds(1)
                scene.world.createItemOnBeltLike(basinOutputPos, Direction.SOUTH, slices)
                scene.idleSeconds(1)
                scene.world.createItemOnBeltLike(basinOutputPos, Direction.SOUTH, slices)
            }
        }
    }

}