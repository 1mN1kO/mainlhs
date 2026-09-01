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
public class AuthorsScreen extends Screen {

    private record AuthorEntry(String roleRu, String roleEn, String name) {}

    private static final AuthorEntry[] ENTRIES = {
            new AuthorEntry("Основатель", "Founder", "Beer_Borman"),
            new AuthorEntry("Владелец хостинга", "Hosting Owner", "ElgerElg"),
            new AuthorEntry("Куратор", "Curator", "TablurExpan"),
            new AuthorEntry("Технический Администратор", "Technical Administrator", "1mN1kO")
    };

    private final Screen parent;
    private float time = 0.0f;
    private float fadeIn = 0.0f;
    private CustomButton backButton;

    public AuthorsScreen(Screen parent) {
        super(Component.literal("Authors"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        backButton = new CustomButton(
                16, 14,
                90, 20,
                Component.literal(MainMenuScreen.isRussian() ? "НАЗАД" : "BACK"),
                btn -> closeScreen(),
                false
        );
        this.addRenderableWidget(backButton);
        applyAnimations();
    }

    @Override
    public void tick() {
        time += 0.05f;
        fadeIn = Math.min(fadeIn + 0.06f, 1.0f);
        applyAnimations();
    }

    private void applyAnimations() {
        if (backButton != null) {
            backButton.setAnimationAlpha(fadeIn);
            backButton.setSlideOffset((1.0f - easeOutCubic(fadeIn)) * 10.0f);
        }
    }

    private static float easeOutCubic(float x) {
        float inv = 1.0f - x;
        return 1.0f - inv * inv * inv;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        float renderTime = time + partialTick * 0.05f;
        MenuBackgroundRenderer.render(gfx, this.width, this.height, renderTime, fadeIn, mouseX, mouseY);
        drawContent(gfx);
        RenderSystem.enableBlend();
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
    }

    private void drawContent(GuiGraphics gfx) {
        Font font = this.font;
        int cx = this.width / 2;
        float alpha = easeOutCubic(fadeIn);
        int baseAlpha = (int) (alpha * 255);
        boolean ru = MainMenuScreen.isRussian();

        // 1. Header Title
        int titleY = Math.max(20, (int) (this.height * 0.12f));
        String title = ru ? "СОЗДАТЕЛИ ПРОЕКТА" : "PROJECT AUTHORS";
        int titleW = font.width(title);

        int glowAlpha = (int) (alpha * 120);
        int shadowColor = (glowAlpha << 24) | 0xFF2A35;
        gfx.drawString(font, title, cx - titleW / 2, titleY + 1, shadowColor, false);
        gfx.drawString(font, title, cx - titleW / 2, titleY, (baseAlpha << 24) | 0xFFF2F4, false);

        String subtitle = "LAST HUMAN STRONGHOLD";
        int subAlpha = (int) (alpha * 150);
        gfx.drawCenteredString(font, subtitle, cx, titleY + 12, (subAlpha << 24) | 0xD4757C);

        // 2. Central Tactical Card
        int cardWidth = Math.min(380, this.width - 32);
        int cardX = cx - cardWidth / 2;
        int cardY = titleY + 30;

        int rowHeight = 24;
        int cardHeight = ENTRIES.length * rowHeight + 14;

        // Card Glass Background
        int bgAlpha = (int) (110 * alpha);
        gfx.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, (bgAlpha << 24) | 0x0A0406);

        // Card Border with subtle glow
        int borderAlpha = (int) (65 * alpha);
        int borderColor = (borderAlpha << 24) | 0x66181E;
        gfx.fill(cardX, cardY, cardX + cardWidth, cardY + 1, borderColor);
        gfx.fill(cardX, cardY + cardHeight - 1, cardX + cardWidth, cardY + cardHeight, borderColor);
        gfx.fill(cardX, cardY, cardX + 1, cardY + cardHeight, borderColor);
        gfx.fill(cardX + cardWidth - 1, cardY, cardX + cardWidth, cardY + cardHeight, borderColor);

        // Tactical Corner Highlights on Card
        int cornerAlpha = (int) (180 * alpha);
        int cornerColor = (cornerAlpha << 24) | 0xFF4450;
        int tickLen = 6;
        gfx.fill(cardX, cardY, cardX + tickLen, cardY + 1, cornerColor);
        gfx.fill(cardX, cardY, cardX + 1, cardY + tickLen, cornerColor);
        gfx.fill(cardX + cardWidth - tickLen, cardY, cardX + cardWidth, cardY + 1, cornerColor);
        gfx.fill(cardX + cardWidth - 1, cardY, cardX + cardWidth, cardY + tickLen, cornerColor);
        gfx.fill(cardX, cardY + cardHeight - 1, cardX + tickLen, cardY + cardHeight, cornerColor);
        gfx.fill(cardX, cardY + cardHeight - tickLen, cardX + 1, cardY + cardHeight, cornerColor);
        gfx.fill(cardX + cardWidth - tickLen, cardY + cardHeight - 1, cardX + cardWidth, cardY + cardHeight, cornerColor);
        gfx.fill(cardX + cardWidth - 1, cardY + cardHeight - tickLen, cardX + cardWidth, cardY + cardHeight, cornerColor);

        // Entries inside card
        int curY = cardY + 8;
        for (int i = 0; i < ENTRIES.length; i++) {
            AuthorEntry entry = ENTRIES[i];
            String role = ru ? entry.roleRu() : entry.roleEn();
            String name = entry.name();

            // Left: Role (with crimson accent)
            int roleAlpha = (int) (alpha * 190);
            int roleColor = (roleAlpha << 24) | 0xE65A65;
            gfx.drawString(font, role, cardX + 14, curY + 4, roleColor, false);

            // Right: Name (bright crisp white/off-white)
            int nameAlpha = (int) (alpha * 240);
            int nameColor = (nameAlpha << 24) | 0xFFFFFF;
            int nameW = font.width(name);
            gfx.drawString(font, name, cardX + cardWidth - 14 - nameW, curY + 4, nameColor, false);

            // Subtle Row Divider
            if (i < ENTRIES.length - 1) {
                int lineAlpha = (int) (25 * alpha);
                gfx.fill(cardX + 10, curY + rowHeight - 1, cardX + cardWidth - 10, curY + rowHeight, (lineAlpha << 24) | 0xFFFFFF);
            }

            curY += rowHeight;
        }
    }

    private void closeScreen() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent == null ? new MainMenuScreen() : parent);
        }
    }

    @Override
    public void onClose() {
        closeScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}