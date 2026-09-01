package com.mainlhs.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UpdateInfoScreen extends Screen {

    private final String info;
    private float time = 0.0f;
    private float fadeIn = 0.0f;

    public UpdateInfoScreen(String info) {
        super(Component.literal("Updates"));
        this.info = info == null ? "No update info available" : info;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 + 50;
        this.addRenderableWidget(new CustomButton(
                cx - 60, y, 120, 22,
                Component.literal(MainMenuScreen.isRussian() ? "ЗАКРЫТЬ" : "CLOSE"),
                btn -> this.minecraft.setScreen(new MainMenuScreen()),
                false
        ));
    }

    @Override
    public void tick() {
        time += 0.05f;
        fadeIn = Math.min(fadeIn + 0.08f, 1.0f);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        float renderTime = time + partialTick * 0.05f;
        MenuBackgroundRenderer.render(gfx, this.width, this.height, renderTime, fadeIn, mouseX, mouseY);

        Font font = this.font;
        int cx = this.width / 2;
        int cy = this.height / 2;

        int cardW = 320;
        int cardH = 150;
        int cardX = cx - cardW / 2;
        int cardY = cy - 75;

        int bgAlpha = (int) (120 * fadeIn);
        gfx.fill(cardX, cardY, cardX + cardW, cardY + cardH, (bgAlpha << 24) | 0x0A0406);

        int borderAlpha = (int) (65 * fadeIn);
        int borderColor = (borderAlpha << 24) | 0x66181E;
        gfx.fill(cardX, cardY, cardX + cardW, cardY + 1, borderColor);
        gfx.fill(cardX, cardY + cardH - 1, cardX + cardW, cardY + cardH, borderColor);
        gfx.fill(cardX, cardY, cardX + 1, cardY + cardH, borderColor);
        gfx.fill(cardX + cardW - 1, cardY, cardX + cardW, cardY + cardH, borderColor);

        String title = MainMenuScreen.isRussian() ? "ИНФОРМАЦИЯ ОБ ОБНОВЛЕНИИ" : "UPDATE INFORMATION";
        int titleAlpha = (int) (240 * fadeIn);
        gfx.drawCenteredString(font, title, cx, cardY + 14, (Mth.clamp(titleAlpha, 0, 255) << 24) | 0xFFE0E5);

        String[] lines = info.split("\\n");
        int textAlpha = (int) (180 * fadeIn);
        for (int i = 0; i < lines.length; i++) {
            gfx.drawCenteredString(font, lines[i], cx, cardY + 34 + i * 14, (Mth.clamp(textAlpha, 0, 255) << 24) | 0xDDCCCC);
        }

        RenderSystem.enableBlend();
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
