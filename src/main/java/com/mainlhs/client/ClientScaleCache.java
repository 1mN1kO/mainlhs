package com.mainlhs.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class ClientScaleCache {

    private static final Map<UUID, Float> SIZES = new ConcurrentHashMap<>();

    private ClientScaleCache() {}

    public static float get(UUID playerId) {
        return SIZES.getOrDefault(playerId, 1.0f);
    }

    public static void set(UUID playerId, float sizeParam) {
        if (Math.abs(sizeParam - 1.0f) < 0.0001f) {
            SIZES.remove(playerId);
        } else {
            SIZES.put(playerId, sizeParam);
        }
    }

    public static void clear(UUID playerId) {
        SIZES.remove(playerId);
    }
}
