package com.mainlhs.compat;

import com.mainlhs.MainLHS;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class LittleTilesCompat {

    private static final String LITTLE_TILES_CLASS = "team.creative.littletiles.LittleTiles";
    private static final String CONFIG_CLASS = "team.creative.littletiles.common.config.LittleTilesConfig";

    private LittleTilesCompat() {}

    public static void ensureConfigLoaded() {
        if (!ModList.get().isLoaded("littletiles")) {
            return;
        }

        try {
            Class<?> littleTilesClass = Class.forName(LITTLE_TILES_CLASS);
            Field configField = littleTilesClass.getDeclaredField("CONFIG");
            configField.setAccessible(true);

            Object config = configField.get(null);
            if (config != null) {
                ensureRenderingThreadCount(config);
                return;
            }

            Class<?> configClass = Class.forName(CONFIG_CLASS);
            Constructor<?> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            config = constructor.newInstance();
            configField.set(null, config);
            ensureRenderingThreadCount(config);

            MainLHS.LOGGER.info("Initialized LittleTiles CONFIG early for modpack compatibility");
        } catch (ClassNotFoundException ignored) {
            // LittleTiles is not present in this environment.
        } catch (Throwable error) {
            MainLHS.LOGGER.warn("Failed to initialize LittleTiles CONFIG early", error);
        }
    }

    private static void ensureRenderingThreadCount(Object config) throws ReflectiveOperationException {
        Field renderingField = config.getClass().getField("rendering");
        Object rendering = renderingField.get(config);
        if (rendering == null) {
            return;
        }

        Field threadCountField = rendering.getClass().getField("renderingThreadCount");
        int threadCount = threadCountField.getInt(rendering);
        if (threadCount < 1) {
            threadCountField.setInt(rendering, 2);
            MainLHS.LOGGER.warn("LittleTiles renderingThreadCount was invalid, reset to 2");
        }
    }
}
