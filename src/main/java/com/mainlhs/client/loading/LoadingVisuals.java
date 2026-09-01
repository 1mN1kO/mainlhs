package com.mainlhs.client.loading;

import com.mainlhs.client.screen.MainMenuScreen;
import com.mainlhs.client.screen.MenuBackgroundRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class LoadingVisuals {

    private LoadingVisuals() {}

    public static void renderFullScreen(
            GuiGraphics gfx,
            Font font,
            int width,
            int height,
            float time,
            String status,
            float progress
    ) {
        renderScreen(gfx, font, width, height, time, status);
    }

    public static void renderOverlay(
            GuiGraphics gfx,
            Font font,
            int width,
            int height,
            float time,
            String status,
            float progress,
            boolean showProgress,
            int bottomFadeHeight
    ) {
        renderScreen(gfx, font, width, height, time, status);
    }

    private static void renderScreen(
            GuiGraphics gfx,
            Font font,
            int width,
            int height,
            float time,
            String status
    ) {
        // 1. Full Atmospheric Background with Live Animated Particles & Glow
        MenuBackgroundRenderer.render(gfx, width, height, time, 1.0f);

        // 2. Center Minimalist Animated Status
        int cx = width / 2;
        int cy = height / 2 - 14;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        String displayStatus = formatStatus(status, time);

        int glowColor = 0x99FF2A35;
        int textColor = 0xFFF2F5;

        gfx.drawCenteredString(font, displayStatus, cx, cy + 1, glowColor);
        gfx.drawCenteredString(font, displayStatus, cx, cy, textColor);

        // Subtle glowing loading wave dots directly underneath the status
        renderPulsingDots(gfx, cx, cy + 14, time);
    }

    private static void renderPulsingDots(GuiGraphics gfx, int cx, int y, float time) {
        int dotCount = 5;
        int gap = 8;
        int startX = cx - (dotCount - 1) * gap / 2;

        for (int i = 0; i < dotCount; i++) {
            float phase = time * 5.0f - i * 0.8f;
            float pulse = (float) (Math.sin(phase) * 0.5f + 0.5f);
            int alpha = (int) (40 + pulse * 215);
            int color = (Mth.clamp(alpha, 0, 255) << 24) | 0xFF3545;
            int x = startX + i * gap;
            gfx.fill(x - 1, y - 1, x + 2, y + 2, color);
        }
    }

    private static String formatStatus(String status, float time) {
        if (status == null || status.isBlank()) {
            int dots = ((int) (time * 2.8f) % 3) + 1;
            return (MainMenuScreen.isRussian() ? "Подключение" : "Connecting") + ".".repeat(dots);
        }

        String lower = status.toLowerCase();
        int dots = ((int) (time * 2.8f) % 3) + 1;
        String dotStr = ".".repeat(dots);

        if (lower.contains("подключ") || lower.contains("connect")) {
            return (MainMenuScreen.isRussian() ? "Подключение" : "Connecting") + dotStr;
        } else if (lower.contains("данных") || lower.contains("receiving")) {
            return (MainMenuScreen.isRussian() ? "Получение данных" : "Receiving data") + dotStr;
        } else if (lower.contains("построен") || lower.contains("building") || lower.contains("мир")) {
            return (MainMenuScreen.isRussian() ? "Построение мира" : "Building world") + dotStr;
        } else if (lower.contains("ресурс") || lower.contains("resource")) {
            return (MainMenuScreen.isRussian() ? "Подготовка ресурсов" : "Preparing resources") + dotStr;
        }

        return status;
    }
}
