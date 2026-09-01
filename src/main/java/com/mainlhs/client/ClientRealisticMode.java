package com.mainlhs.client;

import com.mainlhs.MainLHS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientRealisticMode {

    private static boolean enabled;
    private static float strength;
    private static float speedAmount;

    private ClientRealisticMode() {
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean inWorld = minecraft != null && minecraft.level != null && minecraft.player != null;
        float targetStrength = enabled && inWorld ? 1.0f : 0.0f;
        strength += (targetStrength - strength) * 0.16f;
        if (strength < 0.001f) {
            strength = 0.0f;
        }

        if (!inWorld) {
            speedAmount = 0.0f;
            return;
        }

        double horizontalSpeed = minecraft.player.getDeltaMovement().horizontalDistance();
        float targetSpeed = Mth.clamp((float) horizontalSpeed * 8.0f, 0.0f, 1.0f);
        speedAmount += (targetSpeed - speedAmount) * 0.20f;
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRender(minecraft)) {
            return;
        }

        Player player = minecraft.player;
        float tick = player.tickCount + (float) event.getPartialTick();
        float move = Math.max(speedAmount, player.isSprinting() ? 0.65f : 0.0f);
        float breathing = (float) Math.sin(tick * 0.16f) * 0.28f;
        float runRoll = (float) Math.sin(tick * 0.72f) * move * 1.65f;
        float runPitch = (float) Math.sin(tick * 0.88f + 1.1f) * move * 0.75f;
        float runYaw = (float) Math.sin(tick * 0.58f) * move * 0.50f;

        event.setRoll(event.getRoll() + (breathing + runRoll) * strength);
        event.setPitch(event.getPitch() + runPitch * strength);
        event.setYaw(event.getYaw() + runYaw * strength);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRender(minecraft)) {
            return;
        }

        event.setFOV(event.getFOV() + (7.0f + speedAmount * 4.0f) * strength);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRender(minecraft)) {
            return;
        }

        GuiGraphics gfx = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int alpha = (int) (strength * 255.0f);
        int barAlpha = (int) (strength * 92.0f);

        gfx.fill(0, 0, width, Math.max(12, height / 18), argb(barAlpha, 0x000000));
        gfx.fill(0, height - Math.max(12, height / 18), width, height, argb(barAlpha, 0x000000));
        gfx.fill(0, 0, Math.max(10, width / 42), height, argb((int) (strength * 38.0f), 0x000000));
        gfx.fill(width - Math.max(10, width / 42), 0, width, height, argb((int) (strength * 38.0f), 0x000000));

        int textColor = argb(alpha, 0xE8E8E8);
        int accentColor = argb(alpha, 0xFF3030);
        boolean recBlink = ((minecraft.player.tickCount / 10) % 2) == 0;

        gfx.drawString(minecraft.font, "BODYCAM", 12, 10, textColor, false);
        gfx.drawString(minecraft.font, "MAINLHS-01", 12, 22, argb((int) (alpha * 0.65f), 0xB8B8B8), false);
        if (recBlink) {
            gfx.fill(width - 54, 12, width - 48, 18, accentColor);
        }
        gfx.drawString(minecraft.font, "REC", width - 44, 10, accentColor, false);

        int centerX = width / 2;
        int centerY = height / 2;
        int lineColor = argb((int) (alpha * 0.22f), 0xFFFFFF);
        gfx.fill(centerX - 18, centerY, centerX - 6, centerY + 1, lineColor);
        gfx.fill(centerX + 6, centerY, centerX + 18, centerY + 1, lineColor);
        gfx.fill(centerX, centerY - 18, centerX + 1, centerY - 6, lineColor);
        gfx.fill(centerX, centerY + 6, centerX + 1, centerY + 18, lineColor);
    }

    private static boolean shouldRender(Minecraft minecraft) {
        return strength > 0.001f
                && minecraft != null
                && minecraft.player != null
                && minecraft.level != null
                && minecraft.options.getCameraType().isFirstPerson();
    }

    private static int argb(int alpha, int rgb) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (rgb & 0xFFFFFF);
    }
}
