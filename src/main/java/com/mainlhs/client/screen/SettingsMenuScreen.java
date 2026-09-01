package com.mainlhs.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mainlhs.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OnlineOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SettingsMenuScreen extends Screen {

    private final Screen parent;
    private float time = 0.0f;
    private float fadeIn = 0.0f;
    private float titleGlow = 0.0f;
    private final List<AbstractWidget> menuWidgets = new ArrayList<>();

    private CustomButton backButton;
    private LanguageToggleWidget languageToggle;

    private static final int WIDGET_WIDTH = 200;
    private static final int WIDGET_HEIGHT = 24;
    private static final int COL_GAP = 20;
    private static final int ROW_GAP = 28;

    public SettingsMenuScreen(Screen parent) {
        super(Component.literal("Settings Menu"));
        this.parent = parent;
    }

    private void openVideoSettings(Minecraft minecraft) {
        try {
            Class<?> sodiumClass = Class.forName("me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI");
            java.lang.reflect.Constructor<?> constructor = sodiumClass.getConstructor(Screen.class);
            Screen sodiumScreen = (Screen) constructor.newInstance(this);
            minecraft.setScreen(sodiumScreen);
            return;
        } catch (Throwable ignored) {}

        try {
            Class<?> embeddiumClass = Class.forName("org.embeddedt.embeddium.client.gui.options.EmbeddiumVideoOptionsScreen");
            java.lang.reflect.Constructor<?> constructor = embeddiumClass.getConstructor(Screen.class);
            Screen embeddiumScreen = (Screen) constructor.newInstance(this);
            minecraft.setScreen(embeddiumScreen);
            return;
        } catch (Throwable ignored) {}

        minecraft.setScreen(new VideoSettingsScreen(this, minecraft.options));
    }

    @Override
    protected void init() {
        Minecraft minecraft = this.minecraft;
        if (minecraft == null) return;

        menuWidgets.clear();

        int leftX = this.width / 2 - WIDGET_WIDTH - COL_GAP / 2;
        int rightX = this.width / 2 + COL_GAP / 2;
        int startY = Math.max(70, (int) (this.height * 0.20f));
        int rowGap = Math.max(24, Math.min(ROW_GAP, (this.height - startY - 44) / 7));

        backButton = new CustomButton(
                16, 14,
                90, 20,
                Component.literal(text("НАЗАД", "BACK")),
                btn -> onClose(),
                false
        );
        this.addRenderableWidget(backButton);

        // COLUMN 1
        double initialFovVal = (minecraft.options.fov().get() - 30.0) / (110.0 - 30.0);
        CustomSlider fovSlider = new CustomSlider(
                leftX, startY, WIDGET_WIDTH, WIDGET_HEIGHT,
                text("Поле зрения: ", "FOV: "),
                initialFovVal,
                false,
                30.0,
                110.0,
                val -> {
                    int mappedVal = (int) Math.round(30.0 + val * (110.0 - 30.0));
                    minecraft.options.fov().set(mappedVal);
                }
        );
        menuWidgets.add(fovSlider);
        this.addRenderableWidget(fovSlider);

        addButton(leftX, startY + rowGap, text("ВИДЕО", "VIDEO"),
                btn -> openVideoSettings(minecraft));

        addButton(leftX, startY + rowGap * 2, text("УПРАВЛЕНИЕ", "CONTROLS"),
                btn -> minecraft.setScreen(new ControlsScreen(this, minecraft.options)));

        addButton(leftX, startY + rowGap * 3, text("ЧАТ", "CHAT"),
                btn -> minecraft.setScreen(new ChatOptionsScreen(this, minecraft.options)));

        addButton(leftX, startY + rowGap * 4, text("СКИН", "SKIN"),
                btn -> minecraft.setScreen(new SkinCustomizationScreen(this, minecraft.options)));

        addButton(leftX, startY + rowGap * 5, text("РЕСУРСПАКИ", "RESOURCE PACKS"),
                btn -> minecraft.setScreen(createPackScreen(minecraft)));

        // COLUMN 2
        addButton(rightX, startY, text("ЗВУК", "SOUND"),
                btn -> minecraft.setScreen(new SoundOptionsScreen(this, minecraft.options)));

        addButton(rightX, startY + rowGap, text("МЫШЬ", "MOUSE"),
                btn -> minecraft.setScreen(new MouseSettingsScreen(this, minecraft.options)));

        addButton(rightX, startY + rowGap * 2, text("ЯЗЫК", "LANGUAGE"),
                btn -> minecraft.setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager())));

        addButton(rightX, startY + rowGap * 3, text("СПЕЦИАЛЬНЫЕ ВОЗМОЖНОСТИ", "ACCESSIBILITY"),
                btn -> minecraft.setScreen(new AccessibilityOptionsScreen(this, minecraft.options)));

        addButton(rightX, startY + rowGap * 4, text("СЕТЬ", "ONLINE"),
                btn -> minecraft.setScreen(OnlineOptionsScreen.createOnlineOptionsScreen(minecraft, this, minecraft.options)));

        languageToggle = new LanguageToggleWidget(
                this.width - 80, 14,
                () -> this.minecraft.setScreen(new SettingsMenuScreen(parent))
        );
        this.addRenderableWidget(languageToggle);

        applyAnimations();
        languageToggle.setFadeIn(fadeIn);
    }

    private void addButton(int x, int y, String label, net.minecraft.client.gui.components.Button.OnPress onPress) {
        CustomButton button = new CustomButton(x, y, WIDGET_WIDTH, WIDGET_HEIGHT, Component.literal(label), onPress, false);
        menuWidgets.add(button);
        this.addRenderableWidget(button);
    }

    private String text(String ru, String en) {
        return MainMenuScreen.isRussian() ? ru : en;
    }

    private Screen createPackScreen(Minecraft minecraft) {
        PackRepository repository = minecraft.getResourcePackRepository();
        return new PackSelectionScreen(
                repository,
                updatedRepository -> {
                    minecraft.options.updateResourcePacks(updatedRepository);
                    minecraft.reloadResourcePacks();
                    minecraft.setScreen(new SettingsMenuScreen(parent));
                },
                minecraft.getResourcePackDirectory(),
                Component.literal(text("РЕСУРСПАКИ", "RESOURCE PACKS"))
        );
    }

    private void applyAnimations() {
        if (backButton != null) {
            backButton.setAnimationAlpha(fadeIn);
            backButton.setSlideOffset((1.0f - fadeIn) * 10.0f);
        }

        for (int i = 0; i < menuWidgets.size(); i++) {
            float delay = i * 0.025f;
            float rawProgress = Math.max(0f, Math.min((fadeIn - 0.05f - delay) * 3.2f, 1.0f));
            float alpha = easeOutCubic(rawProgress);

            AbstractWidget widget = menuWidgets.get(i);
            if (widget instanceof CustomButton cb) {
                cb.setAnimationAlpha(alpha);
                cb.setSlideOffset((1.0f - alpha) * 12.0f);
            } else if (widget instanceof CustomSlider cs) {
                cs.setAnimationAlpha(alpha);
                cs.setSlideOffset((1.0f - alpha) * 12.0f);
            }
            widget.setAlpha(alpha);
        }
    }

    private static float easeOutCubic(float x) {
        float inv = 1.0f - x;
        return 1.0f - inv * inv * inv;
    }

    @Override
    public void tick() {
        time += 0.05f;
        fadeIn = Math.min(fadeIn + 0.06f, 1.0f);
        titleGlow = (float) (Math.sin(time * 1.4f) * 0.5 + 0.5);

        applyAnimations();

        if (languageToggle != null) {
            languageToggle.setFadeIn(fadeIn);
            languageToggle.tick();
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        float renderTime = time + partialTick * 0.05f;
        MenuBackgroundRenderer.render(gfx, this.width, this.height, renderTime, fadeIn, mouseX, mouseY);
        drawHeader(gfx);
        RenderSystem.enableBlend();
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {}

    private void drawHeader(GuiGraphics gfx) {
        Font font = this.font;
        int cx = this.width / 2;
        int titleY = Math.max(14, (int) (this.height * 0.06f));

        float alpha = easeOutCubic(fadeIn);
        int baseAlpha = (int) (alpha * 255);
        int glowAlpha = (int) (alpha * (80 + titleGlow * 90));

        String title = "LAST HUMAN STRONGHOLD";
        int titleW = font.width(title);

        int shadowColor = (Mth.clamp(glowAlpha, 0, 255) << 24) | 0xFF2A35;
        gfx.drawString(font, title, cx - titleW / 2, titleY + 1, shadowColor, false);

        int titleColor = (Mth.clamp(baseAlpha, 0, 255) << 24) | 0xFFF2F4;
        gfx.drawString(font, title, cx - titleW / 2, titleY, titleColor, false);

        String sub = text("— ЦЕНТР НАСТРОЕК —", "— CONTROL CENTER —");
        int subAlpha = (int) (alpha * 160);
        gfx.drawCenteredString(font, sub, cx, titleY + 12, (Mth.clamp(subAlpha, 0, 255) << 24) | 0xD4757C);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.options.save();
            ClientConfig.save();
            if (this.parent != null) {
                this.minecraft.setScreen(this.parent);
            } else {
                this.minecraft.setScreen(new MainMenuScreen());
            }
        }
    }
}
