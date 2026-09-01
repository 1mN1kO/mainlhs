package com.mainlhs.client.screen;

import com.mainlhs.client.sound.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomButton extends Button {

    private final boolean dangerStyle;
    private boolean primaryStyle = false;

    private float hoverAnim = 0.0f;
    private float clickAnim = 0.0f;
    private float animAlpha = 1.0f;
    private float slideOffset = 0.0f;
    private boolean hoverSoundPlayed = false;
    private boolean disableHoverEffects = false;

    private long lastRenderTime = System.currentTimeMillis();

    public CustomButton(int x, int y, int width, int height, Component text, OnPress onPress, boolean dangerStyle) {
        super(x, y, width, height, text, btn -> {
            if (btn instanceof CustomButton custom) {
                custom.clickAnim = 1.0f;
            }
            onPress.onPress(btn);
        }, DEFAULT_NARRATION);
        this.dangerStyle = dangerStyle;
    }

    public CustomButton setPrimary(boolean primary) {
        this.primaryStyle = primary;
        return this;
    }

    public void setAnimationAlpha(float alpha) {
        this.animAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        this.visible = this.animAlpha > 0.02f;
        this.active = this.visible;
    }

    public void setSlideOffset(float offset) {
        this.slideOffset = offset;
    }

    public void setDisableHoverEffects(boolean disable) {
        this.disableHoverEffects = disable;
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if (!this.visible || animAlpha <= 0.02f) {
            return;
        }

        // Delta time for butter-smooth 60/144/240+ FPS animations
        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastRenderTime) / 1000.0f, 0.1f);
        lastRenderTime = now;
        float time = MenuBackgroundRenderer.getTime();

        boolean hovered = this.isHovered() && !this.disableHoverEffects;

        // Smooth hover interpolation
        float targetHover = hovered ? 1.0f : 0.0f;
        float hoverSpeed = hovered ? 12.0f : 8.0f;
        hoverAnim += (targetHover - hoverAnim) * Math.min(1.0f, dt * hoverSpeed);

        // Click rebound animation
        if (clickAnim > 0.0f) {
            clickAnim = Math.max(0.0f, clickAnim - dt * 6.0f);
        }

        // Sound feedback
        if (hovered && !hoverSoundPlayed) {
            ModSounds.playHoverSound();
            hoverSoundPlayed = true;
        } else if (!hovered) {
            hoverSoundPlayed = false;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int x = this.getX();
        int y = this.getY() + (int) slideOffset + (clickAnim > 0.05f ? 1 : 0);
        int w = this.getWidth();
        int h = this.getHeight();

        float pulse = (float) (Math.sin(time * 3.2f) * 0.5f + 0.5f);

        // 1. Radiant Bloom Aura Behind Button
        if (primaryStyle || hoverAnim > 0.02f) {
            float strength = (primaryStyle ? 0.35f : 0.0f) + hoverAnim * 0.85f;
            int auraLayers = primaryStyle && hovered ? 3 : 2;
            for (int l = 1; l <= auraLayers; l++) {
                int spread = l * 2;
                int auraAlpha = (int) (strength * (18.0f + pulse * 12.0f) / l * animAlpha);
                int auraRgb = dangerStyle ? 0xAA1818 : (primaryStyle ? 0xDD2532 : 0x882025);
                gfx.fill(x - spread, y - spread, x + w + spread, y + h + spread, (Mth.clamp(auraAlpha, 0, 255) << 24) | auraRgb);
            }
        }

        // 2. Translucent Glass Body — BRIGHT, stands out against dark background
        int baseAlpha = (int) ((140.0f + hoverAnim * 80.0f + (primaryStyle ? 35.0f : 0.0f)) * animAlpha);
        int baseRgb;
        if (dangerStyle) {
            baseRgb = hovered ? 0x3A0A0E : 0x200508;
        } else if (primaryStyle) {
            baseRgb = hovered ? 0x420E14 : 0x280810;
        } else {
            baseRgb = hovered ? 0x320C10 : 0x1C0810;
        }
        gfx.fill(x, y, x + w, y + h, (Mth.clamp(baseAlpha, 0, 255) << 24) | baseRgb);

        // 3. Special Hover Shimmer Sweep for Primary (Play) Button
        if (primaryStyle && hoverAnim > 0.1f) {
            int sweepW = 35;
            int totalTravel = w + sweepW * 2;
            int sweepX = x + ((int) (time * 160.0f) % totalTravel) - sweepW;
            int startX = Math.max(x, sweepX);
            int endX = Math.min(x + w, sweepX + sweepW);

            if (endX > startX) {
                int shimmerAlpha = (int) (hoverAnim * 55.0f * animAlpha);
                gfx.fill(startX, y + 1, endX, y + h - 1, (shimmerAlpha << 24) | 0xFF8899);
            }
        }

        // 4. Left Indicator Accent Bar & Animated Tactical Chevron
        int barWidth = 3;
        int barAlpha = (int) ((primaryStyle ? 230 + hoverAnim * 25 : 160 + hoverAnim * 95) * animAlpha);
        int barRgb;
        if (dangerStyle) {
            barRgb = hovered ? 0xFF4048 : 0xCC2830;
        } else if (primaryStyle) {
            barRgb = hovered ? 0xFF5D6A : 0xEE3D48;
        } else {
            barRgb = hovered ? 0xFF5560 : 0xBB3540;
        }
        int barHeight = Math.max(4, (int) (h * (primaryStyle ? 0.6f + hoverAnim * 0.4f : 0.4f + hoverAnim * 0.6f)));
        int barY = y + (h - barHeight) / 2;
        gfx.fill(x, barY, x + barWidth, barY + barHeight, (Mth.clamp(barAlpha, 0, 255) << 24) | barRgb);

        // Primary Play Button: Animated glowing chevron marker on hover
        if (primaryStyle && hoverAnim > 0.15f) {
            int chevAlpha = (int) (hoverAnim * (220 + pulse * 35) * animAlpha);
            int chevColor = (Mth.clamp(chevAlpha, 0, 255) << 24) | 0xFF6677;
            int chevX = x + 10 + (int) (hoverAnim * 2.0f);
            int chevY = y + h / 2;
            gfx.fill(chevX, chevY - 3, chevX + 1, chevY + 4, chevColor);
            gfx.fill(chevX + 1, chevY - 2, chevX + 2, chevY + 3, chevColor);
            gfx.fill(chevX + 2, chevY - 1, chevX + 3, chevY + 2, chevColor);
            gfx.fill(chevX + 3, chevY, chevX + 4, chevY + 1, chevColor);
        }

        // 5. Clean 1px Outline — bright and visible
        int borderAlpha = (int) ((130.0f + hoverAnim * 125.0f + (primaryStyle ? 45.0f : 0.0f)) * animAlpha);
        int borderRgb = dangerStyle ? (hovered ? 0xFF3838 : 0xAA2028)
                : (primaryStyle ? (hovered ? 0xFF5D6E : 0xDD3E4A)
                : (hovered ? 0xDD4852 : 0x883038));
        int borderColor = (Mth.clamp(borderAlpha, 0, 255) << 24) | borderRgb;

        gfx.fill(x, y, x + w, y + 1, borderColor); // Top
        gfx.fill(x, y + h - 1, x + w, y + h, borderColor); // Bottom
        gfx.fill(x, y, x + 1, y + h, borderColor); // Left
        gfx.fill(x + w - 1, y, x + w, y + h, borderColor); // Right

        // 6. Tactical Illuminated Corner Accents
        if (hoverAnim > 0.08f) {
            int cornerAlpha = (int) (hoverAnim * 245.0f * animAlpha);
            int cornerColor = (Mth.clamp(cornerAlpha, 0, 255) << 24) | (dangerStyle ? 0xFF5555 : (primaryStyle ? 0xFF8899 : 0xFF6677));
            gfx.fill(x + w - 5, y, x + w, y + 1, cornerColor);
            gfx.fill(x + w - 1, y, x + w, y + 5, cornerColor);
            gfx.fill(x, y + h - 1, x + 5, y + h, cornerColor);
            gfx.fill(x, y + h - 5, x + 1, y + h, cornerColor);
        }

        // 7. Glowing Bottom Accent Line on hover
        if (hovered && (primaryStyle || hoverAnim > 0.5f)) {
            int lineAlpha = (int) (hoverAnim * 200.0f * animAlpha);
            int lineW = (int) (w * (0.4f + hoverAnim * 0.4f));
            int lineX = x + (w - lineW) / 2;
            gfx.fill(lineX, y + h - 1, lineX + lineW, y + h, (Mth.clamp(lineAlpha, 0, 255) << 24) | (primaryStyle ? 0xFF5D6A : 0xEE4550));
        }

        // 8. Text Rendering — bright and crisp
        Font font = font();
        int textShift = (int) (hoverAnim * (primaryStyle ? 4.0f : 3.0f));
        int textAlpha = (int) ((240.0f + hoverAnim * 15.0f) * animAlpha);
        int textColor;
        if (dangerStyle) {
            textColor = hovered ? 0xFFFFFF : 0xEECCCC;
        } else if (primaryStyle) {
            textColor = hovered ? 0xFFFFFF : 0xFFEEF0;
        } else {
            textColor = hovered ? 0xFFFFFF : 0xEEDDDF;
        }

        int textX = x + w / 2 + textShift;
        int textY = y + (h - 8) / 2;

        if (hoverAnim > 0.15f || primaryStyle) {
            int shadowAlpha = (int) ((primaryStyle ? 50 : 0 + hoverAnim * 110.0f) * animAlpha);
            int shadowColor = (Mth.clamp(shadowAlpha, 0, 255) << 24) | (dangerStyle ? 0xBB0000 : (primaryStyle ? 0xAA0018 : 0x880000));
            gfx.drawCenteredString(font, this.getMessage(), textX + 1, textY + 1, shadowColor);
        }

        gfx.drawCenteredString(font, this.getMessage(), textX, textY, (Mth.clamp(textAlpha, 0, 255) << 24) | textColor);
    }

    private Font font() {
        return Minecraft.getInstance().font;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        ModSounds.playClickSound();
    }
}
