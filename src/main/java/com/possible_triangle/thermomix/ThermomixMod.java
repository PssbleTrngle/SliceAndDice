package com.possible_triangle.thermomix;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ThermomixMod.MOD_ID)
public class ThermomixMod {

    public static final String MOD_ID = "thermomix";

    public ThermomixMod() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Content.INSTANCE.register(eventBus);
    }

}