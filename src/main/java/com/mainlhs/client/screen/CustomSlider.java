package com.mainlhs.client.screen;

import com.mainlhs.client.sound.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class CustomSlider extends AbstractSliderButton {

    private final Consumer<Double> onValueChanged;
    private final String prefix;
    private final boolean percent;
    private final double minValue;
    private final double maxValue;

    private float animationHover = 0.0f;
    private float animationAlpha = 1.0f;
    private float slideOffset = 0.0f;
    private boolean hoverSoundPlayed = false;
    private long lastRenderTime = System.currentTimeMillis();

    public CustomSlider(int x, int y, int width, int height, String prefix, double initialValue, boolean percent, double minValue, double maxValue, Consumer<Double> onValueChanged) {
        super(x, y, width, height, Component.empty(), initialValue);
        this.prefix = prefix;
        this.percent = percent;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.onValueChanged = onValueChanged;
        updateMessage();
    }

    public CustomSlider(int x, int y, int width, int height, String prefix, double initialValue, boolean percent, Consumer<Double> onValueChanged) {
        this(x, y, width, height, prefix, initialValue, percent, 0.0, 1.0, onValueChanged);
    }

    @Override
    protected void updateMessage() {
        String valText;
        if (percent) {
            valText = Math.round(this.value * 100.0) + "%";
        } else {
            int mappedVal = (int) Math.round(minValue + this.value * (maxValue - minValue));
            if (mappedVal == 70) {
                valText = mappedVal + " (" + (MainMenuScreen.isRussian() ? "Норм" : "Normal") + ")";
            } else if (mappedVal == 110) {
                valText = "Quake Pro";
            } else {
                valText = String.valueOf(mappedVal);
            }
        }
        this.setMessage(Component.literal(prefix + valText));
    }

    @Override
    protected void applyValue() {
        if (onValueChanged != null) {
            onValueChanged.accept(this.value);
        }
    }

    public void setAnimationAlpha(float alpha) {
        this.animationAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        this.setAlpha(this.animationAlpha);
        this.visible = this.animationAlpha > 0.02f;
        this.active = this.visible;
    }

    public void setSlideOffset(float offset) {
        this.slideOffset = offset;
    }

    @Override
    public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if (animationAlpha < 0.02f) return;

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastRenderTime) / 1000.0f, 0.1f);
        lastRenderTime = now;

        boolean hovered = isHoveredOrFocused();
        if (hovered && !hoverSoundPlayed) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(ModSounds.BUTTON_HOVER.get(), 0.3f, 1.05f)
            );
            hoverSoundPlayed = true;
        } else if (!hovered) {
            hoverSoundPlayed = false;
        }

        float targetHover = hovered ? 1.0f : 0.0f;
        animationHover += (targetHover - animationHover) * Math.min(1.0f, dt * 10.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int x = getX();
        int y = getY() + (int) slideOffset;
        int w = getWidth();
        int h = getHeight();

        // 1. Background Frame
        int bgAlpha = (int) ((80.0f + animationHover * 50.0f) * animationAlpha);
        gfx.fill(x, y, x + w, y + h, (bgAlpha << 24) | 0x0E0507);

        // 2. Track Fill
        int trackMargin = 3;
        int trackX = x + trackMargin;
        int trackY = y + trackMargin;
        int trackW = w - trackMargin * 2;
        int trackH = h - trackMargin * 2;

        int fillW = (int) (this.value * trackW);

        // Track empty background
        gfx.fill(trackX, trackY, trackX + trackW, trackY + trackH, ((int) (60 * animationAlpha) << 24) | 0x1A090C);

        // Track filled progress
        if (fillW > 0) {
            int fillAlpha = (int) ((160 + animationHover * 60) * animationAlpha);
            gfx.fill(trackX, trackY, trackX + fillW, trackY + trackH, (fillAlpha << 24) | 0x8C161E);
            // Highlight line on top of progress
            gfx.fill(trackX, trackY, trackX + fillW, trackY + 1, ((int) (200 * animationAlpha) << 24) | 0xFF4D5A);
        }

        // 3. Thumb Handle
        int handleW = 4;
        int handleX = trackX + fillW - handleW / 2;
        handleX = Mth.clamp(handleX, trackX, trackX + trackW - handleW);

        int handleAlpha = (int) (255 * animationAlpha);
        int handleRgb = hovered ? 0xFF5566 : 0xDD3340;
        gfx.fill(handleX, y + 1, handleX + handleW, y + h - 1, (handleAlpha << 24) | handleRgb);
        // Handle center bright pin
        gfx.fill(handleX + 1, y + 2, handleX + handleW - 1, y + h - 2, (handleAlpha << 24) | 0xFFFFFF);

        // 4. Subtle Border
        int borderAlpha = (int) ((45.0f + animationHover * 80.0f) * animationAlpha);
        int borderColor = (borderAlpha << 24) | 0x66181E;
        gfx.fill(x, y, x + w, y + 1, borderColor);
        gfx.fill(x, y + h - 1, x + w, y + h, borderColor);
        gfx.fill(x, y, x + 1, y + h, borderColor);
        gfx.fill(x + w - 1, y, x + w, y + h, borderColor);

        // 5. Centered Value Text
        Font font = Minecraft.getInstance().font;
        int textAlpha = (int) ((190.0f + animationHover * 65.0f) * animationAlpha);
        int textColor = hovered ? 0xFFFFFF : 0xDFD6D8;
        int textY = y + (h - 8) / 2;

        gfx.drawCenteredString(font, getMessage(), x + w / 2, textY, (Mth.clamp(textAlpha, 0, 255) << 24) | textColor);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(ModSounds.BUTTON_CLICK.get(), 0.92f, 1.0f));
    }
}
