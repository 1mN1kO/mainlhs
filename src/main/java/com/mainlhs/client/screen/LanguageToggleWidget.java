package com.mainlhs.client.screen;

import com.mainlhs.client.sound.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageToggleWidget extends AbstractWidget {

    private final Runnable onLanguageChanged;
    private float fadeIn = 0.0f;
    private float slideAnim = MainMenuScreen.isRussian() ? 0.0f : 1.0f;
    private float pulseAnim = 0.0f;
    private boolean ruHoverPlayed = false;
    private boolean enHoverPlayed = false;
    private long lastRenderTime = System.currentTimeMillis();

    public LanguageToggleWidget(int x, int y, Runnable onLanguageChanged) {
        super(x, y, 68, 18, Component.empty());
        this.onLanguageChanged = onLanguageChanged;
    }

    public void setFadeIn(float fadeIn) {
        this.fadeIn = Mth.clamp(fadeIn, 0.0f, 1.0f);
    }

    public void tick() {
        // Kept for compatibility if tick called
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if (fadeIn <= 0.02f) {
            return;
        }

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastRenderTime) / 1000.0f, 0.1f);
        lastRenderTime = now;

        // Smoothly interpolate slider position
        float targetSlide = MainMenuScreen.isRussian() ? 0.0f : 1.0f;
        slideAnim += (targetSlide - slideAnim) * Math.min(1.0f, dt * 14.0f);

        if (pulseAnim > 0.0f) {
            pulseAnim = Math.max(0.0f, pulseAnim - dt * 4.0f);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        int halfW = (w - 4) / 2;
        boolean ruHovered = mouseX >= x && mouseX < x + w / 2 && mouseY >= y && mouseY < y + h;
        boolean enHovered = mouseX >= x + w / 2 && mouseX < x + w && mouseY >= y && mouseY < y + h;

        if (ruHovered && !ruHoverPlayed) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(ModSounds.BUTTON_HOVER.get(), 0.3f, 1.1f)
            );
            ruHoverPlayed = true;
        } else if (!ruHovered) {
            ruHoverPlayed = false;
        }

        if (enHovered && !enHoverPlayed) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(ModSounds.BUTTON_HOVER.get(), 0.3f, 1.1f)
            );
            enHoverPlayed = true;
        } else if (!enHovered) {
            enHoverPlayed = false;
        }

        // 1. Pill Background
        int bgAlpha = (int) (120 * fadeIn);
        gfx.fill(x, y, x + w, y + h, (bgAlpha << 24) | 0x0A0406);

        // 2. Subtle 1px Border
        int borderAlpha = (int) (60 * fadeIn);
        int borderColor = (borderAlpha << 24) | 0x66181E;
        gfx.fill(x, y, x + w, y + 1, borderColor);
        gfx.fill(x, y + h - 1, x + w, y + h, borderColor);
        gfx.fill(x, y, x + 1, y + h, borderColor);
        gfx.fill(x + w - 1, y, x + w, y + h, borderColor);

        // 3. Sliding Active Highlight Pill
        int indicatorX = x + 2 + (int) (slideAnim * halfW);
        int indAlpha = (int) ((160 + pulseAnim * 80) * fadeIn);
        indAlpha = Mth.clamp(indAlpha, 0, 255);
        int indColor = (indAlpha << 24) | 0x8B1820;
        gfx.fill(indicatorX, y + 2, indicatorX + halfW, y + h - 2, indColor);

        // Bottom accent glow line on active pill
        int glowLineAlpha = (int) (220 * fadeIn);
        gfx.fill(indicatorX + 2, y + h - 3, indicatorX + halfW - 2, y + h - 2, (glowLineAlpha << 24) | 0xFF4450);

        // 4. Text Labels
        Font font = Minecraft.getInstance().font;
        int textY = y + (h - 8) / 2;

        boolean isRu = MainMenuScreen.isRussian();
        int ruTextColor = colorForText(isRu, ruHovered, fadeIn);
        int enTextColor = colorForText(!isRu, enHovered, fadeIn);

        int ruCenterX = x + 2 + halfW / 2;
        int enCenterX = x + 2 + halfW + halfW / 2;

        gfx.drawCenteredString(font, "RU", ruCenterX, textY, ruTextColor);
        gfx.drawCenteredString(font, "EN", enCenterX, textY, enTextColor);
    }

    private int colorForText(boolean active, boolean hovered, float fade) {
        int alpha = (int) ((active ? 255 : (hovered ? 210 : 120)) * fade);
        int rgb = active ? 0xFFFFFF : (hovered ? 0xDDDDDD : 0x888888);
        return (Mth.clamp(alpha, 0, 255) << 24) | rgb;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int halfW = getWidth() / 2;
        if (mouseX >= getX() && mouseX < getX() + halfW) {
            if (!MainMenuScreen.isRussian()) {
                MainMenuScreen.setRussian(true);
                pulseAnim = 1.0f;
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(ModSounds.BUTTON_CLICK.get(), 0.95f, 1.0f)
                );
                if (onLanguageChanged != null) {
                    onLanguageChanged.run();
                }
            }
        } else if (mouseX >= getX() + halfW && mouseX < getX() + getWidth()) {
            if (MainMenuScreen.isRussian()) {
                MainMenuScreen.setRussian(false);
                pulseAnim = 1.0f;
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(ModSounds.BUTTON_CLICK.get(), 0.95f, 1.0f)
                );
                if (onLanguageChanged != null) {
                    onLanguageChanged.run();
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
