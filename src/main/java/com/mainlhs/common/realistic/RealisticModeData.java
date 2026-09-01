package com.mainlhs.common.realistic;

import net.minecraft.world.entity.player.Player;

public final class RealisticModeData {

    private static final String TAG = "mainlhs_realistic_bodycam";

    private RealisticModeData() {
    }

    public static boolean isEnabled(Player player) {
        return player != null && player.getPersistentData().getBoolean(TAG);
    }

    public static boolean toggle(Player player) {
        boolean enabled = !isEnabled(player);
        setEnabled(player, enabled);
        return enabled;
    }

    public static void setEnabled(Player player, boolean enabled) {
        if (player == null) {
            return;
        }
        if (enabled) {
            player.getPersistentData().putBoolean(TAG, true);
        } else {
            player.getPersistentData().remove(TAG);
        }
    }
}
