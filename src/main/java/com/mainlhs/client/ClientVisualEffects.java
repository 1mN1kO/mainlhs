package com.mainlhs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.mainlhs.MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientVisualEffects {

    private ClientVisualEffects() {}

    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;
        PostChain current = minecraft.gameRenderer.currentEffect();
        if (current != null && current.getName() != null && current.getName().contains("lhs_graphics")) {
            minecraft.gameRenderer.shutdownEffect();
        }
    }
}
