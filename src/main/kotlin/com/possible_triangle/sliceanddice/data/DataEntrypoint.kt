package com.possible_triangle.sliceanddice.data

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.MOD_ID
import com.possible_triangle.sliceanddice.PonderScenes
import com.possible_triangle.sliceanddice.index.SDTags
import com.simibubi.create.AllEntityTypes
import com.tterrag.registrate.providers.ProviderType
import net.createmod.ponder.foundation.PonderIndex
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

@EventBusSubscriber
object DataEntrypoint {
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun gatherData(event: GatherDataEvent) {
        PonderIndex.registerAll()

        REGISTRATE.addRawLang("sliceanddice.tooltip.rotationDirection", "Rotation Direction")
        REGISTRATE.addRawLang(
            "sliceanddice.gui.contraptions.wrong_direction",
            "It appears that this %s is rotating in the _wrong direction_.",
        )

        REGISTRATE.addRawLang("$MOD_ID.recipe.assembly.slicer", "Cut with Slicer")
        REGISTRATE.addRawLang("$MOD_ID.recipe.slicer", "Slicer")

        REGISTRATE.addDataGenerator(ProviderType.LANG) { provider ->
            PonderScenes.setup()
            PonderIndex.getLangAccess().provideLang(MOD_ID, provider::add)
        }

        REGISTRATE.addDataGenerator(ProviderType.RECIPE, CompatRecipes::generate)

        REGISTRATE.registerSprinklers()

        REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS) {
            it.addTag(SDTags.CONTRAPTIONS)
                .add(AllEntityTypes.CONTROLLED_CONTRAPTION.value())
                .add(AllEntityTypes.CARRIAGE_CONTRAPTION.value())
                .add(AllEntityTypes.GANTRY_CONTRAPTION.value())
                .add(AllEntityTypes.ORIENTED_CONTRAPTION.value())
        }
    }
}
