[FARMERS_DELIGHT]: https://www.curseforge.com/minecraft/mc-mods/farmers-delight
[KOTLIN_FORGE_FORGE]: https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge
[CREATE]: https://www.curseforge.com/minecraft/mc-mods/create
[OVERWEIGHT_FARMING]: https://www.curseforge.com/minecraft/mc-mods/overweight-farming
[NEAPOLITAN]: https://www.curseforge.com/minecraft/mc-mods/neapolitan
[DOWNLOAD]: https://www.curseforge.com/minecraft/mc-mods/slice-and-dice/files
[CURSEFORGE]: https://www.curseforge.com/minecraft/mc-mods/slice-and-dice
[MODRINTH]: https://modrinth.com/mod/slice-and-dice
[ISSUES]: https://github.com/PssbleTrngle/SliceAndDice/issues

<!-- modrinth_exclude.start -->
# Create Slice &  Dice
[![Release](https://img.shields.io/github/v/release/PssbleTrngle/SliceAndDice?label=Version&sort=semver)][DOWNLOAD]
[![Downloads](http://cf.way2muchnoise.eu/full_659674_downloads.svg)][CURSEFORGE]
[![Version](http://cf.way2muchnoise.eu/versions/659674.svg)][DOWNLOAD]
[![Issues](https://img.shields.io/github/issues/PssbleTrngle/SliceAndDice?label=Issues)][ISSUES]
[![Modrinth](https://img.shields.io/modrinth/dt/GmjmRQ0A?color=green&logo=modrinth&logoColor=green)][MODRINTH]
<!-- modrinth_exclude.end -->

[![](https://img.shields.io/badge/REQUIRES%20KOTLIN%20FOR%20FORGE-blue?logo=curseforge&labelColor=gray&style=for-the-badge)][KOTLIN_FORGE_FORGE]
[![](https://img.shields.io/badge/REQUIRES%20CREATE-gold?logo=curseforge&labelColor=gray&style=for-the-badge)][CREATE]

### Slicer

This mod enables a variety of features to create better compatibility between mostly [Farmer's Delight][FARMERS_DELIGHT] and [Create][CREATE].
While it is designed to work with Farmer's Delight, it does work without it and also adds some compatibility features for other mods.

![](https://raw.githubusercontent.com/PssbleTrngle/SliceAndDice/1.18.x/screenshots/slicer.png)

### Automatic Cutting

The Main feature of the mod is the _Slicer_, a machine similar to the _Mechanical Mixer_ or _Mechanical Press_ from Create.
It automatically registers all cutting recipes from Farmer's Delight. In that sense, it is an automatic _Cutting Board_.  
In order to use it, the correct tool has to be placed into the machine, using `Right-Click`. 
By default, only knives and axes are allowed, but this behaviour can be overwritten by modifying the `sliceanddice:allowed_tools` item tag.

### Automatic Cooking

All recipes from Farmer's delight requiring the Cooking Pot are added as heated mixing recipes.

![](https://raw.githubusercontent.com/PssbleTrngle/SliceAndDice/1.18.x/screenshots/cooking.png)

### Sprinkler

The Sprinkler is a block which, when provided with a fluid using a pipe, will distribute it in a small area below.  
Different fluids can have different effects.

- Lava applies a small amount of fire damage to entities below 
- Water makes the area below wet, making the world think it's raining there.
- Potions apply their affect for a short duration to entities below
- Liquid Fertilizer, a new fluid, applies a bonemeal affect to blocks.

The latter is meant to enable growing of _Banana Fonds_ from [Neapolitan][NEAPOLITAN] without being dependent on the weather, but it could possibly have other effects on other mods too.

![](https://raw.githubusercontent.com/PssbleTrngle/SliceAndDice/1.18.x/screenshots/sprinkler.png)

### Overweight Farming

If present, some compatibility features for [Overweight Farming][OVERWEIGHT_FARMING] is added.  
This includes waxing recipes using the deployer, 
as well as showing the axe-stripping of overweight crops in JEI.

![](https://raw.githubusercontent.com/PssbleTrngle/SliceAndDice/1.18.x/screenshots/strip.png)
![](https://raw.githubusercontent.com/PssbleTrngle/SliceAndDice/1.18.x/screenshots/wax.png)
