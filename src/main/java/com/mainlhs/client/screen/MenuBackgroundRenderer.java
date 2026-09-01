package com.mainlhs.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class MenuBackgroundRenderer {

    private static final long START_TIME = System.currentTimeMillis();
    private static final int PARTICLE_COUNT = 70;

    private MenuBackgroundRenderer() {}

    public static float getTime() {
        return (System.currentTimeMillis() - START_TIME) / 1000.0f;
    }

    public static void render(GuiGraphics gfx, int width, int height, float time, float fade) {
        render(gfx, width, height, time, fade, width / 2, height / 2);
    }

    public static void render(GuiGraphics gfx, int width, int height, float time, float fade, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float clampedFade = Mth.clamp(fade, 0.0f, 1.0f);
        float px = ((float) mouseX / Math.max(1, width) - 0.5f) * 20.0f;
        float py = ((float) mouseY / Math.max(1, height) - 0.5f) * 14.0f;

        // 1. Solid deep black base
        gfx.fill(0, 0, width, height, 0xFF020101);

        // 2. Smooth radial black-to-crimson gradient (per-scanline for zero banding)
        renderRadialGradient(gfx, width, height, time, clampedFade, px, py);

        // 3. Floating embers
        renderEmbers(gfx, width, height, time, clampedFade, px, py);

        // 4. Corner accents
        renderCornerAccents(gfx, width, height, clampedFade);
    }

    /**
     * Per-scanline radial gradient from deep black edges to warm crimson center.
     * Every horizontal line computes its own distance from center, producing
     * a perfectly smooth continuous transition with zero visible rings or bands.
     */
    private static void renderRadialGradient(GuiGraphics gfx, int width, int height, float time, float fade, float px, float py) {
        if (fade <= 0.01f) return;

        float cx = width * 0.5f + px * 0.4f;
        float cy = height * 0.42f + py * 0.4f;
        float pulse = (float) (Math.sin(time * 0.8f) * 0.5f + 0.5f);

        // Aspect-corrected radii — how far the glow reaches
        float radiusX = width * 0.7f;
        float radiusY = height * 0.65f;

        // Render every scanline individually for perfectly smooth gradient
        for (int y = 0; y < height; y++) {
            float dy = (y - cy) / radiusY;
            float dy2 = dy * dy;

            if (dy2 >= 1.0f) continue; // fully outside vertical reach

            // Horizontal span at this scanline (ellipse equation)
            float horizFactor = (float) Math.sqrt(1.0f - dy2);
            int halfSpan = (int) (radiusX * horizFactor);

            int left = (int) cx - halfSpan;
            int right = (int) cx + halfSpan;

            // Clamp to screen
            left = Math.max(0, left);
            right = Math.min(width, right);
            if (right <= left) continue;

            // Distance from center vertically (0 = center, 1 = edge)
            float vertDist = Math.abs(dy);

            // Smoothstep falloff: strongest at center, fades to zero at edges
            float vertFade = 1.0f - vertDist;
            vertFade = vertFade * vertFade * (3.0f - 2.0f * vertFade); // smoothstep

            // Base intensity at this scanline
            float intensity = vertFade * (0.28f + pulse * 0.08f) * fade;

            // Color: dark crimson, not too bright
            int red = (int) (intensity * 140.0f);
            int green = (int) (intensity * 12.0f);
            int blue = (int) (intensity * 18.0f);
            int alpha = (int) (intensity * 255.0f);

            red = Mth.clamp(red, 0, 255);
            green = Mth.clamp(green, 0, 255);
            blue = Mth.clamp(blue, 0, 255);
            alpha = Mth.clamp(alpha, 0, 255);

            if (alpha <= 0) continue;

            int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
            gfx.fill(left, y, right, y + 1, color);
        }
    }

    private static void renderEmbers(GuiGraphics gfx, int width, int height, float time, float fade, float px, float py) {
        if (fade <= 0.01f) return;

        float totalH = height + 40.0f;
        float totalW = width + 60.0f;

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float rx = fract((i + 1) * 0.618033988749895);
            float ry = fract((i + 1) * 0.754877666246693);
            float rd = fract((i + 1) * 0.569840296317204);

            float depth = 0.20f + rd * 0.80f;
            float speedY = 12.0f + rd * 26.0f;
            float windDrift = ((float) Math.sin((i + 1) * 13.37f)) * (3.0f + rd * 7.0f);

            float startY = ry * totalH;
            float travelY = (startY + time * speedY) % totalH;
            float y = height + 20.0f - travelY + (py * depth * 0.7f);

            float baseX = rx * totalW;
            float swayFreq = 0.55f + rd * 0.6f;
            float swayAmp = 12.0f + rd * 14.0f;
            float sway = (float) Math.sin(time * swayFreq + i * 2.39f) * swayAmp;

            float travelX = (baseX + sway + time * windDrift) % totalW;
            if (travelX < 0) travelX += totalW;
            float x = travelX - 30.0f + (px * depth * 0.7f);

            if (y < -10 || y > height + 10 || x < -10 || x > width + 10) continue;

            float flicker = (float) (Math.sin(time * 3.8f + i * 2.1f) * 0.3f + 0.7f);
            int alpha = (int) ((35.0f + depth * 140.0f) * flicker * fade);
            alpha = Mth.clamp(alpha, 0, 255);

            int size = depth > 0.80f ? 2 : 1;

            int colorType = i % 4;
            int rgb;
            if (colorType == 0) {
                rgb = 0xDD9944; // Warm amber
            } else if (colorType == 1) {
                rgb = 0xCC2E38; // Deep crimson
            } else if (colorType == 2) {
                rgb = 0xAA2028; // Blood red
            } else {
                rgb = 0x882020; // Dark ember
            }

            int color = (alpha << 24) | rgb;
            gfx.fill((int) x, (int) y, (int) x + size, (int) y + size, color);

            if (depth > 0.80f && alpha > 60) {
                int haloAlpha = alpha / 4;
                gfx.fill((int) x - 1, (int) y - 1, (int) x + size + 1, (int) y + size + 1, (haloAlpha << 24) | rgb);
            }
        }
    }

    private static float fract(double v) {
        return (float) (v - Math.floor(v));
    }

    private static void renderCornerAccents(GuiGraphics gfx, int width, int height, float fade) {
        int alpha = (int) (30.0f * fade);
        if (alpha <= 0) return;
        int color = (alpha << 24) | 0x661820;

        int margin = 12;
        int len = 16;

        gfx.fill(margin, margin, margin + len, margin + 1, color);
        gfx.fill(margin, margin, margin + 1, margin + len, color);

        gfx.fill(width - margin - len, margin, width - margin, margin + 1, color);
        gfx.fill(width - margin - 1, margin, width - margin, margin + len, color);

        gfx.fill(margin, height - margin - 1, margin + len, height - margin, color);
        gfx.fill(margin, height - margin - len, margin + 1, height - margin, color);

        gfx.fill(width - margin - len, height - margin - 1, width - margin, height - margin, color);
        gfx.fill(width - margin - 1, height - margin - len, width - margin, height - margin, color);
    }
}
