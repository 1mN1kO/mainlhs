package com.mainlhs.client.config;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class ClientConfig {

    private static final String FILE_NAME = "mainlhs.properties";
    private static final String KEY_MENU_VOL = "menuMusicVolume";
    private static final String KEY_GRAPHICS_MODE = "graphicsMode";

    private static float menuMusicVolume = 0.7f;
    private static int graphicsMode = 2;

    private ClientConfig() {}

    public static void load() {
        try {
            File gameDir = Minecraft.getInstance().gameDirectory;
            File cfg = new File(new File(gameDir, "config"), FILE_NAME);
            if (!cfg.exists()) return;
            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream(cfg)) {
                p.load(in);
            }
            String v = p.getProperty(KEY_MENU_VOL);
            if (v != null) {
                menuMusicVolume = Float.parseFloat(v);
            }
            String graphics = p.getProperty(KEY_GRAPHICS_MODE);
            if (graphics != null) {
                graphicsMode = clampGraphicsMode(Integer.parseInt(graphics));
            }
        } catch (Throwable ignored) {
            // ignore
        }
    }

    public static void save() {
        try {
            File gameDir = Minecraft.getInstance().gameDirectory;
            File cfgDir = new File(gameDir, "config");
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File cfg = new File(cfgDir, FILE_NAME);
            Properties p = new Properties();
            p.setProperty(KEY_MENU_VOL, Float.toString(menuMusicVolume));
            p.setProperty(KEY_GRAPHICS_MODE, Integer.toString(graphicsMode));
            try (FileOutputStream out = new FileOutputStream(cfg)) {
                p.store(out, "MainLHS client config");
            }
        } catch (IOException ignored) {
        }
    }

    public static float getMenuMusicVolume() {
        return menuMusicVolume;
    }

    public static void setMenuMusicVolume(float vol) {
        menuMusicVolume = Math.max(0f, Math.min(1f, vol));
    }

    public static int getGraphicsMode() {
        return graphicsMode;
    }

    public static void setGraphicsMode(int mode) {
        graphicsMode = clampGraphicsMode(mode);
    }

    public static int nextGraphicsMode() {
        graphicsMode = (graphicsMode + 1) % 4;
        return graphicsMode;
    }

    public static String getGraphicsModeName(boolean russian) {
        return switch (graphicsMode) {
            case 0 -> russian ? "Выкл" : "Off";
            case 1 -> russian ? "Лёгкая" : "Low";
            case 3 -> russian ? "Кино" : "Cinematic";
            default -> russian ? "Баланс" : "Balanced";
        };
    }

    private static int clampGraphicsMode(int mode) {
        return Math.max(0, Math.min(3, mode));
    }
}
