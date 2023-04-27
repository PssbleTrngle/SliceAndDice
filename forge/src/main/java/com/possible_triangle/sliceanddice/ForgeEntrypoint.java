package com.possible_triangle.sliceanddice;

import com.possible_triangle.sliceanddice.platform.ForgeConfig;
import com.possible_triangle.sliceanddice.platform.ForgeRegistriesImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ForgeEntrypoint {

    public ForgeEntrypoint() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((GatherDataEvent event) -> PonderScenes.INSTANCE.register());
        modBus.addListener((FMLClientSetupEvent event) -> PonderScenes.INSTANCE.register());
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> SlicerPartials.INSTANCE::load);

        ForgeConfig.Companion.init();
        ForgeRegistriesImpl.Companion.register(modBus);
    }

}