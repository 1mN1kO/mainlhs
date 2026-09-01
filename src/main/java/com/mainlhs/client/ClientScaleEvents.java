package com.mainlhs.client;

import com.mainlhs.MainLHS;
import com.mainlhs.common.scale.PlayerScaleData;
import com.mainlhs.common.scale.ScaleMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientScaleEvents {

    private ClientScaleEvents() {}

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        float sizeParam = PlayerScaleData.getSize(player);
        if (Math.abs(sizeParam - 1.0f) < 0.0001f) {
            return;
        }

        float factor = ScaleMath.getScaleFactor(sizeParam);
        PoseStack poseStack = event.getPoseStack();
        poseStack.scale(factor, factor, factor);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        float sizeParam = PlayerScaleData.getSize(player);
        if (Math.abs(sizeParam - 1.0f) < 0.0001f) {
            return;
        }

        float factor = 1.0f / ScaleMath.getScaleFactor(sizeParam);
        PoseStack poseStack = event.getPoseStack();
        poseStack.scale(factor, factor, factor);
    }
}
