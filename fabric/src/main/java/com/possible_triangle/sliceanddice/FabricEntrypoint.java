package com.possible_triangle.sliceanddice;

import com.possible_triangle.sliceanddice.platform.FabricRegistriesImpl;
import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Content.INSTANCE.init();

        FabricRegistriesImpl.Companion.register();
    }
}
