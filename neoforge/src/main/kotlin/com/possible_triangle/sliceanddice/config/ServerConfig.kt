package com.possible_triangle.sliceanddice.config

import com.simibubi.create.content.processing.recipe.HeatCondition
import net.neoforged.neoforge.common.ModConfigSpec

class ServerConfig(
    builder: ModConfigSpec.Builder,
) {
    @JvmField
    val harvesterUsesKnife = builder.define("harvester.uses_knife", true)

    val consumeDurability = builder.define("slicer.consume_tool_durability", true)
    val ignoreRotation = builder.define("slicer.ignore_rotation", false)
    val showConvertedRecipes = builder.define("slicer.jei_show_converted_recipes", false)

    val basinCooking = builder.define("basin_cooking.enabled", true)
    val replaceFluidContainers = builder.define("basin_cooking.replace_fluid_containers", true)
    val cookingHeatConditition = builder.defineEnum("basin_cooking.heat_condition", HeatCondition.HEATED)

    val sprinklerCapacity = builder.defineInRange("sprinkler.fluid_capacity", 300, 10, 2048)
    val sprinklerUsage = builder.defineInRange("sprinkler.fluid_per_use", 100, 0, 2048)
    val sprinklerRange = builder.defineInRange("sprinkler.horizontal_range", 5, 1, 32)
}
