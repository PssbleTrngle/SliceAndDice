package com.possible_triangle.sliceanddice;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SliceAndDice.MOD_ID)
public class SliceAndDice {

    public static final String MOD_ID = "sliceanddice";

    public SliceAndDice() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Content.INSTANCE.register(eventBus);
    }

}