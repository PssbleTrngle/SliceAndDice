package com.possible_triangle.sliceanddice;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SliceAndDice.MOD_ID)
public class SliceAndDice {

    public static final String MOD_ID = "sliceanddice";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public SliceAndDice(ModContainer container, IEventBus modBus, Dist dist) {
        REGISTRATE.registerEventListeners(modBus);
        Content.INSTANCE.register(container, modBus);

        if(dist.isClient()) {
            Content.INSTANCE.clientInit();
        }
    }

}