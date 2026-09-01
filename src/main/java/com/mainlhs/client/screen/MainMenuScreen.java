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
public class MainMenuScreen extends Screen {

    private static boolean russian = true;

    private final boolean cinematicIntro;
    private float time = 0.0f;
    private float fadeIn = 0.0f;
    private float titleGlow = 0.0f;
    private final float[] buttonFade = {0f, 0f, 0f, 0f};

    private CustomButton playButton;
    private CustomButton settingsButton;
    private CustomButton authorsButton;
    private CustomButton exitButton;
    private LanguageToggleWidget languageToggle;

    private static final int BTN_WIDTH = 210;
    private static final int BTN_HEIGHT = 26;
    private static final int BTN_GAP = 9;

    private long lastTickTime = System.currentTimeMillis();

    public MainMenuScreen() {
        this(false);
    }

    public MainMenuScreen(boolean cinematicIntro) {
        super(Component.literal("LAST HUMAN STRONGHOLD"));
        this.cinematicIntro = cinematicIntro;
    }

    public static boolean isRussian() {
        return russian;
    }

    public static void setRussian(boolean value) {
        russian = value;
    }

    public static String text(String ru, String en) {
        return russian ? ru : en;
    }

    @Override
    protected void init() {
        if (cinematicIntro && fadeIn < 0.01f) {
            fadeIn = 0.02f;
        }

        int centerX = this.width / 2;
        int startY = Math.max(115, (int) (this.height * 0.44f));

        playButton = new CustomButton(
                centerX - BTN_WIDTH / 2, startY,
                BTN_WIDTH, BTN_HEIGHT,
                Component.literal(text("ИГРАТЬ", "PLAY")),
                btn -> ServerConnector.connect(this),
                false
        ).setPrimary(true);

        settingsButton = new CustomButton(
                centerX - BTN_WIDTH / 2, startY + (BTN_HEIGHT + BTN_GAP),
                BTN_WIDTH, BTN_HEIGHT,
                Component.literal(text("НАСТРОЙКИ", "SETTINGS")),
                btn -> this.minecraft.setScreen(new SettingsMenuScreen(this)),
                false
        );

        authorsButton = new CustomButton(
                centerX - BTN_WIDTH / 2, startY + (BTN_HEIGHT + BTN_GAP) * 2,
                BTN_WIDTH, BTN_HEIGHT,
                Component.literal(text("АВТОРЫ", "AUTHORS")),
                btn -> this.minecraft.setScreen(new AuthorsScreen(this)),
                false
        );

        exitButton = new CustomButton(
                centerX - BTN_WIDTH / 2, startY + (BTN_HEIGHT + BTN_GAP) * 3,
                BTN_WIDTH, BTN_HEIGHT,
                Component.literal(text("ВЫХОД", "EXIT")),
                btn -> this.minecraft.stop(),
                true
        );

        languageToggle = new LanguageToggleWidget(
                this.width - 80, 14,
                () -> this.minecraft.setScreen(new MainMenuScreen())
        );

        this.addRenderableWidget(playButton);
        this.addRenderableWidget(settingsButton);
        this.addRenderableWidget(authorsButton);
        this.addRenderableWidget(exitButton);
        this.addRenderableWidget(languageToggle);

        applyButtonAnimations();
        languageToggle.setFadeIn(fadeIn);
    }

    @Override
    public void tick() {
        time += 0.05f;
        float fadeSpeed = cinematicIntro ? 0.035f : 0.05f;
        fadeIn = Math.min(fadeIn + fadeSpeed, 1.0f);
        titleGlow = (float) (Math.sin(time * 1.4f) * 0.5 + 0.5);

        float buttonStart = cinematicIntro ? 0.25f : 0.15f;
        if (fadeIn > buttonStart) {
            for (int i = 0; i < buttonFade.length; i++) {
                float delay = i * 0.12f;
                float progress = Math.max(0f, Math.min((fadeIn - buttonStart - delay) * 2.8f, 1.0f));
                buttonFade[i] = easeOutCubic(progress);
            }
        }

        applyButtonAnimations();

        if (languageToggle != null) {
            languageToggle.setFadeIn(fadeIn);
            languageToggle.tick();
        }
    }

    private static float easeOutCubic(float x) {
        float inv = 1.0f - x;
        return 1.0f - inv * inv * inv;
    }

    private void applyButtonAnimations() {
        CustomButton[] buttons = {playButton, settingsButton, authorsButton, exitButton};
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            buttons[i].setAnimationAlpha(buttonFade[i]);
            buttons[i].setSlideOffset((1.0f - buttonFade[i]) * 14.0f);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        float renderTime = MenuBackgroundRenderer.getTime();
        MenuBackgroundRenderer.render(gfx, this.width, this.height, renderTime, fadeIn, mouseX, mouseY);

        drawHeader(gfx, renderTime);
        drawFooter(gfx);

        RenderSystem.enableBlend();
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void drawHeader(GuiGraphics gfx, float renderTime) {
        Font font = this.font;
        int cx = this.width / 2;
        int titleY = Math.max(28, (int) (this.height * 0.18f));

        float alpha = easeOutCubic(fadeIn);
        int baseAlpha = (int) (alpha * 255);
        int glowAlpha = (int) (alpha * (75 + titleGlow * 105));

        // Main Title Header
        String title = "LAST HUMAN STRONGHOLD";
        int titleWidth = font.width(title);

        // Soft Red Title Glow Layer
        int shadowColor = (Mth.clamp(glowAlpha, 0, 255) << 24) | 0xFF2A35;
        gfx.drawString(font, title, cx - titleWidth / 2, titleY + 1, shadowColor, false);
        // Foreground Crisp Title
        int titleColor = (Mth.clamp(baseAlpha, 0, 255) << 24) | 0xFFF2F4;
        gfx.drawString(font, title, cx - titleWidth / 2, titleY, titleColor, false);

        // Subtitle / Tagline
        String sub = text("ПОСЛЕДНИЙ ОПЛОТ ЧЕЛОВЕЧЕСТВА", "THE LAST HUMAN STRONGHOLD");
        int subY = titleY + 14;
        int subAlpha = (int) (alpha * 165);
        int subColor = (Mth.clamp(subAlpha, 0, 255) << 24) | 0xD4757C;
        gfx.drawCenteredString(font, sub, cx, subY, subColor);

        // Tactical Horizontal Divider with Center Flare
        int divY = subY + 16;
        int divHalfWidth = Math.min(140, this.width / 4);
        int divAlpha = (int) (alpha * 80);
        int divColor = (divAlpha << 24) | 0x882025;

        gfx.fill(cx - divHalfWidth, divY, cx - 12, divY + 1, divColor);
        gfx.fill(cx + 12, divY, cx + divHalfWidth, divY + 1, divColor);

        // Center tech diamond / dot
        int dotAlpha = (int) (alpha * (180 + titleGlow * 75));
        int dotColor = (Mth.clamp(dotAlpha, 0, 255) << 24) | 0xFF4D5A;
        gfx.fill(cx - 2, divY - 1, cx + 2, divY + 2, dotColor);
    }

    private void drawFooter(GuiGraphics gfx) {
        // Footer left and right removed per user request for ultra-clean minimalist layout
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
