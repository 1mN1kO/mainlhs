package com.mainlhs.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NetworkConnectScreen extends Screen {

    private final Screen parent;
    private EditBox addressBox;
    private Button connectButton;
    private Button cancelButton;

    private float time = 0.0f;
    private float fadeIn = 0.0f;

    public NetworkConnectScreen(Screen parent) {
        super(Component.literal("Network Connect"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 10;

        addressBox = new EditBox(this.minecraft.font, cx - 130, y, 260, 22, Component.literal(""));
        addressBox.setValue(ServerConnector.getServerAddress());
        this.addRenderableWidget(addressBox);

        connectButton = new CustomButton(
                cx - 130, y + 30,
                125, 24,
                Component.literal(MainMenuScreen.isRussian() ? "ПОДКЛЮЧИТЬСЯ" : "CONNECT"),
                btn -> {
                    String val = addressBox.getValue();
                    if (val != null && !val.isBlank()) {
                        ServerConnector.connect(this, val.trim());
                    }
                },
                false
        ).setPrimary(true);
        this.addRenderableWidget(connectButton);

        cancelButton = new CustomButton(
                cx + 5, y + 30,
                125, 24,
                Component.literal(MainMenuScreen.isRussian() ? "ОТМЕНА" : "CANCEL"),
                btn -> {
                    if (this.minecraft != null) {
                        if (this.parent != null) this.minecraft.setScreen(this.parent);
                        else this.minecraft.setScreen(new MainMenuScreen());
                    }
                },
                true
        );
        this.addRenderableWidget(cancelButton);

        setInitialFocus(addressBox);
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

        // Modal Glass Card Box
        int cardW = 290;
        int cardH = 110;
        int cardX = cx - cardW / 2;
        int cardY = cy - 56;

        int bgAlpha = (int) (120 * fadeIn);
        gfx.fill(cardX, cardY, cardX + cardW, cardY + cardH, (bgAlpha << 24) | 0x0A0406);

        int borderAlpha = (int) (65 * fadeIn);
        int borderColor = (borderAlpha << 24) | 0x66181E;
        gfx.fill(cardX, cardY, cardX + cardW, cardY + 1, borderColor);
        gfx.fill(cardX, cardY + cardH - 1, cardX + cardW, cardY + cardH, borderColor);
        gfx.fill(cardX, cardY, cardX + 1, cardY + cardH, borderColor);
        gfx.fill(cardX + cardW - 1, cardY, cardX + cardW, cardY + cardH, borderColor);

        // Header Title
        String title = MainMenuScreen.isRussian() ? "ПРЯМОЕ ПОДКЛЮЧЕНИЕ" : "DIRECT CONNECT";
        int titleAlpha = (int) (240 * fadeIn);
        gfx.drawCenteredString(font, title, cx, cardY + 12, (Mth.clamp(titleAlpha, 0, 255) << 24) | 0xFFE0E5);

        String subtitle = MainMenuScreen.isRussian() ? "Введите адрес сервера (IP:порт):" : "Enter server address (IP:port):";
        int subAlpha = (int) (140 * fadeIn);
        gfx.drawCenteredString(font, subtitle, cx, cardY + 26, (Mth.clamp(subAlpha, 0, 255) << 24) | 0xB09095);

        RenderSystem.enableBlend();
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && this.addressBox != null && this.addressBox.isFocused()) {
            String val = addressBox.getValue();
            if (val != null && !val.isBlank()) {
                ServerConnector.connect(this, val.trim());
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
