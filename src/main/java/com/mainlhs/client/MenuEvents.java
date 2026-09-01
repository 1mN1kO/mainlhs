package com.mainlhs.client;

import com.mainlhs.MainLHS;
import com.mainlhs.client.loading.BrandedLoadingOverlay;
import com.mainlhs.client.loading.LoadingVisuals;
import com.mainlhs.client.screen.IntroLoadingScreen;
import com.mainlhs.client.screen.MainMenuScreen;
import com.mainlhs.client.screen.SettingsMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.BackgroundRendered;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import com.mainlhs.client.screen.MenuBackgroundRenderer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MenuEvents {

    private static final Field LOADING_OVERLAY_RELOAD = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96164_");
    private static final Field LOADING_OVERLAY_ON_FINISH = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96165_");
    private static final Field LOADING_OVERLAY_FADE_IN = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96166_");
    private static final Field LOADING_OVERLAY_PROGRESS = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96167_");
    private static final Field CONNECT_STATUS = ObfuscationReflectionHelper.findField(ConnectScreen.class, "f_95687_");
    private static final Field PROGRESS_HEADER = ObfuscationReflectionHelper.findField(ProgressScreen.class, "f_96506_");
    private static final Field PROGRESS_STAGE = ObfuscationReflectionHelper.findField(ProgressScreen.class, "f_96507_");
    private static final Field PROGRESS_VALUE = ObfuscationReflectionHelper.findField(ProgressScreen.class, "f_96508_");
    private static boolean introShown;

    private MenuEvents() {}

    @SubscribeEvent
    public static void onBackgroundRender(BackgroundRendered event) {
        Screen screen = event.getScreen();
        if (screen instanceof OptionsScreen || screen instanceof OptionsSubScreen ||
            screen instanceof SelectWorldScreen || screen instanceof PackSelectionScreen ||
            screen instanceof net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen) {
            
            if (screen instanceof SettingsMenuScreen) {
                return; // Custom background already drawn
            }
            
            float time = MenuBackgroundRenderer.getTime();
            MenuBackgroundRenderer.render(event.getGuiGraphics(), screen.width, screen.height, time, 1.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Screen screen = event.getScreen() != null ? event.getScreen() : event.getNewScreen();
        if (screen == null) {
            return;
        }

        if (screen instanceof TitleScreen && !(screen instanceof MainMenuScreen)) {
            event.setNewScreen(createReplacementScreen());
        } else if (screen instanceof OptionsScreen && !(screen instanceof SettingsMenuScreen)) {
            if (!(Minecraft.getInstance().screen instanceof SettingsMenuScreen)) {
                event.setNewScreen(new SettingsMenuScreen(Minecraft.getInstance().screen));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        Screen screen = minecraft.screen;
        if (shouldReplaceWithCustomMenu(screen)) {
            minecraft.setScreen(createReplacementScreen());
        }

        ensureBrandedLoadingOverlay(minecraft);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;

        Screen screen = event.getScreen();
        if (screen instanceof ConnectScreen || screen instanceof ProgressScreen || screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen) {
            
            event.setCanceled(true);
            
            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();
            float time = MenuBackgroundRenderer.getTime();
            GuiGraphics gfx = event.getGuiGraphics();

            if (screen instanceof ConnectScreen connectScreen) {
                String status = getComponentText(readComponent(CONNECT_STATUS, connectScreen));
                if (status.isBlank()) {
                    status = MainMenuScreen.isRussian() ? "подключение к серверу" : "connecting to server";
                }
                LoadingVisuals.renderOverlay(gfx, minecraft.font, width, height, time, status, 0.0f, false, 60);
            } else if (screen instanceof ProgressScreen progressScreen) {
                String header = getComponentText(readComponent(PROGRESS_HEADER, progressScreen));
                String stage = getComponentText(readComponent(PROGRESS_STAGE, progressScreen));
                int progressValue = readInt(PROGRESS_VALUE, progressScreen);
                String status = joinStatus(header, stage, MainMenuScreen.isRussian() ? "выполняется операция" : "processing task");
                LoadingVisuals.renderOverlay(gfx, minecraft.font, width, height, time, status, Mth.clamp(progressValue / 100.0f, 0.0f, 1.0f), true, 0);
            } else if (screen instanceof ReceivingLevelScreen) {
                String status = MainMenuScreen.isRussian() ? "получение данных мира" : "receiving world data";
                LoadingVisuals.renderOverlay(gfx, minecraft.font, width, height, time, status, 0.0f, false, 0);
            } else if (screen instanceof LevelLoadingScreen) {
                String status = MainMenuScreen.isRussian() ? "построение мира" : "building world";
                LoadingVisuals.renderOverlay(gfx, minecraft.font, width, height, time, status, 0.0f, false, 0);
            }

            int mouseX = event.getMouseX();
            int mouseY = event.getMouseY();
            for (net.minecraft.client.gui.components.Renderable renderable : screen.renderables) {
                if (renderable instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                    int btnW = Math.min(130, width - 40);
                    int btnH = 22;
                    int btnX = width / 2 - btnW / 2;
                    int btnY = height / 2 + 24;

                    widget.setX(btnX);
                    widget.setY(btnY);
                    widget.setWidth(btnW);
                    widget.setHeight(btnH);

                    renderCustomLoadingButton(gfx, minecraft.font, widget, mouseX, mouseY, time);
                } else {
                    renderable.render(gfx, mouseX, mouseY, event.getPartialTick());
                }
            }
        }
    }

    private static float loadingBtnHoverAnim = 0.0f;
    private static boolean loadingBtnHoverSoundPlayed = false;
    private static long loadingBtnLastRenderTime = System.currentTimeMillis();

    private static void renderCustomLoadingButton(
            GuiGraphics gfx,
            net.minecraft.client.gui.Font font,
            net.minecraft.client.gui.components.AbstractWidget widget,
            int mouseX,
            int mouseY,
            float time
    ) {
        if (!widget.visible) return;

        long now = System.currentTimeMillis();
        float dt = Math.min((now - loadingBtnLastRenderTime) / 1000.0f, 0.1f);
        loadingBtnLastRenderTime = now;

        boolean hovered = widget.isHoveredOrFocused();

        // Custom UI Hover Sound
        if (hovered && !loadingBtnHoverSoundPlayed) {
            com.mainlhs.client.sound.ModSounds.playHoverSound();
            loadingBtnHoverSoundPlayed = true;
        } else if (!hovered) {
            loadingBtnHoverSoundPlayed = false;
        }

        // Smooth Delta-Time Hover Animation
        float targetHover = hovered ? 1.0f : 0.0f;
        float speed = hovered ? 12.0f : 7.0f;
        loadingBtnHoverAnim += (targetHover - loadingBtnHoverAnim) * Math.min(1.0f, dt * speed);

        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();

        float pulse = (float) (Math.sin(time * 3.0f) * 0.5f + 0.5f);

        // 1. Radiant Glow underlay on hover
        if (loadingBtnHoverAnim > 0.02f) {
            int glowAlpha = (int) (loadingBtnHoverAnim * (24.0f + pulse * 14.0f));
            gfx.fill(x - 2, y - 2, x + w + 2, y + h + 2, (glowAlpha << 24) | 0xAA2028);
        }

        // 2. Translucent Glass Base
        int baseAlpha = (int) (130 + loadingBtnHoverAnim * 85);
        int baseRgb = hovered ? 0x220507 : 0x0E0507;
        gfx.fill(x, y, x + w, y + h, (baseAlpha << 24) | baseRgb);

        // 3. Expanding Left Indicator Bar
        int barW = 3;
        int barH = Math.max(4, (int) (h * (0.4f + loadingBtnHoverAnim * 0.6f)));
        int barY = y + (h - barH) / 2;
        int barAlpha = (int) (130 + loadingBtnHoverAnim * 125);
        int barRgb = hovered ? 0xFF3545 : 0xAA2028;
        gfx.fill(x, barY, x + barW, barY + barH, (barAlpha << 24) | barRgb);

        // 4. Glowing 1px Border
        int borderAlpha = (int) (65 + loadingBtnHoverAnim * 150);
        int borderRgb = hovered ? 0xFF3848 : 0x66181E;
        int borderColor = (borderAlpha << 24) | borderRgb;
        gfx.fill(x, y, x + w, y + 1, borderColor);
        gfx.fill(x, y + h - 1, x + w, y + h, borderColor);
        gfx.fill(x, y, x + 1, y + h, borderColor);
        gfx.fill(x + w - 1, y, x + w, y + h, borderColor);

        // 5. Illuminated Corner Highlights
        if (loadingBtnHoverAnim > 0.08f) {
            int cornerAlpha = (int) (loadingBtnHoverAnim * 230);
            int cornerColor = (cornerAlpha << 24) | 0xFFFF4450;
            gfx.fill(x + w - 4, y, x + w, y + 1, cornerColor);
            gfx.fill(x + w - 1, y, x + w, y + 4, cornerColor);
            gfx.fill(x, y + h - 1, x + 4, y + h, cornerColor);
            gfx.fill(x, y + h - 4, x + 1, y + h, cornerColor);
        }

        // 6. Text with smooth slide and glowing drop shadow
        Component msg = widget.getMessage();
        String text = msg == null ? (MainMenuScreen.isRussian() ? "ОТМЕНА" : "CANCEL") : msg.getString();
        if (text.equalsIgnoreCase("cancel") && MainMenuScreen.isRussian()) {
            text = "ОТМЕНА";
        }
        int textShift = (int) (loadingBtnHoverAnim * 3.0f);
        int textColor = hovered ? 0xFFFFFF : 0xDDCCD0;
        int textX = x + w / 2 + textShift;
        int textY = y + (h - 8) / 2;

        if (loadingBtnHoverAnim > 0.15f) {
            int shadowAlpha = (int) (loadingBtnHoverAnim * 95);
            gfx.drawCenteredString(font, text, textX + 1, textY + 1, (shadowAlpha << 24) | 0x880000);
        }
        gfx.drawCenteredString(font, text, textX, textY, textColor);
    }

    @SubscribeEvent
    public static void onScreenMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (screen instanceof ConnectScreen || screen instanceof ProgressScreen || screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen) {
            double mx = event.getMouseX();
            double my = event.getMouseY();
            for (net.minecraft.client.gui.components.Renderable renderable : screen.renderables) {
                if (renderable instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                    if (widget.visible && widget.active && widget.isMouseOver(mx, my)) {
                        com.mainlhs.client.sound.ModSounds.playClickSound();
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        // Now empty since logic moved to Pre
    }

    private static Screen createReplacementScreen() {
        if (!introShown) {
            introShown = true;
            return new IntroLoadingScreen();
        }
        return new MainMenuScreen();
    }

    private static boolean shouldReplaceWithCustomMenu(Screen screen) {
        return screen instanceof TitleScreen && !(screen instanceof MainMenuScreen);
    }

    private static void ensureBrandedLoadingOverlay(Minecraft minecraft) {
        if (!(minecraft.getOverlay() instanceof LoadingOverlay overlay) || overlay instanceof BrandedLoadingOverlay) {
            return;
        }

        try {
            ReloadInstance reload = (ReloadInstance) LOADING_OVERLAY_RELOAD.get(overlay);
            @SuppressWarnings("unchecked")
            Consumer<Optional<Throwable>> onFinish = (Consumer<Optional<Throwable>>) LOADING_OVERLAY_ON_FINISH.get(overlay);
            boolean fadeIn = LOADING_OVERLAY_FADE_IN.getBoolean(overlay);
            float progress = LOADING_OVERLAY_PROGRESS.getFloat(overlay);

            BrandedLoadingOverlay customOverlay = new BrandedLoadingOverlay(minecraft, reload, onFinish, fadeIn);
            customOverlay.setCurrentProgress(progress);
            minecraft.setOverlay(customOverlay);
        } catch (IllegalAccessException exception) {
            MainLHS.LOGGER.error("Failed to replace loading overlay", exception);
        }
    }

    private static Component readComponent(Field field, Object target) {
        try {
            Object value = field.get(target);
            return value instanceof Component component ? component : Component.empty();
        } catch (IllegalAccessException exception) {
            return Component.empty();
        }
    }

    private static int readInt(Field field, Object target) {
        try {
            return field.getInt(target);
        } catch (IllegalAccessException exception) {
            return 0;
        }
    }

    private static String getComponentText(Component component) {
        return component == null ? "" : component.getString().trim();
    }

    private static String joinStatus(String first, String second, String fallback) {
        if (!first.isBlank() && !second.isBlank()) {
            return first + " / " + second;
        }
        if (!first.isBlank()) {
            return first;
        }
        if (!second.isBlank()) {
            return second;
        }
        return fallback;
    }
}
