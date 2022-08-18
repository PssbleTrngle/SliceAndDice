package com.possible_triangle.sliceanddice;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SliceAndDice.MOD_ID)
public class SliceAndDice {

    public static final String MOD_ID = "sliceanddice";
    public static final Logger LOGGER = LogManager.getLogger();

    public SliceAndDice() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Content.INSTANCE.register(eventBus);
    }

}