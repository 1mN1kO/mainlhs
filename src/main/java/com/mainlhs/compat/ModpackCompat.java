package com.mainlhs.compat;

import com.mainlhs.MainLHS;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModpackCompat {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        LittleTilesCompat.ensureConfigLoaded();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientSetup(FMLClientSetupEvent event) {
        LittleTilesCompat.ensureConfigLoaded();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        LittleTilesCompat.ensureConfigLoaded();
    }
}
