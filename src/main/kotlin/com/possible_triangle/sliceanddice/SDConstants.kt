package com.possible_triangle.sliceanddice

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager

const val MOD_ID = "sliceanddice"

val LOGGER = LogManager.getLogger("Slice & Dice")

fun modLoc(path: String) = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
