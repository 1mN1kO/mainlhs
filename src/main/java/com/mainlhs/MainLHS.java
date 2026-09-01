package com.mainlhs;

import com.mainlhs.client.sound.ModSounds;
import com.mainlhs.common.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MainLHS.MOD_ID)
public class MainLHS {

    public static final String MOD_ID = "mainlhs";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public MainLHS() {
        IEventBus modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        ModSounds.SOUND_EVENTS.register(modBus);
        com.mainlhs.server.ModItems.register();
        modBus.addListener(this::onCommonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::onClientSetup);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        LOGGER.info("LAST HUMAN STRONGHOLD common setup loaded");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Load client config (menu music volume, etc.)
        com.mainlhs.client.config.ClientConfig.load();
        LOGGER.info("LAST HUMAN STRONGHOLD custom menu loaded");
    }
}
