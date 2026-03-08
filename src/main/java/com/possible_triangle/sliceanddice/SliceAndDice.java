package com.possible_triangle.sliceanddice;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SliceAndDice implements ModInitializer, ClientModInitializer, DataGeneratorEntrypoint {

    public static final String MOD_ID = "sliceanddice";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        Content.INSTANCE.register();
    }

    @Override
    public void onInitializeClient() {
        SlicerPartials.INSTANCE.load();
        PonderScenes.INSTANCE.setup();
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        Content.INSTANCE.registerData(generator);
    }
}