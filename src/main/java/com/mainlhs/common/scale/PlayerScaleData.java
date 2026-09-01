package com.mainlhs.common.scale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class PlayerScaleData {

    private static final String NBT_KEY = "GenericScale";

    private PlayerScaleData() {}

    public static float getSize(Player player) {
        if (player.level().isClientSide) {
            return DistExecutor.unsafeCallWhenOn(
                    Dist.CLIENT,
                    () -> () -> com.mainlhs.client.ClientPacketHandler.getPlayerSize(player)
            );
        }

        CompoundTag tag = player.getPersistentData();
        if (tag.contains(NBT_KEY)) {
            return tag.getFloat(NBT_KEY);
        }
        return 1.0f;
    }

    public static void setSize(Player player, float sizeParam) {
        CompoundTag tag = player.getPersistentData();
        if (Math.abs(sizeParam - 1.0f) < 0.0001f) {
            tag.remove(NBT_KEY);
        } else {
            tag.putFloat(NBT_KEY, sizeParam);
        }

        player.refreshDimensions();

        if (player instanceof ServerPlayer serverPlayer) {
            com.mainlhs.common.network.NetworkHandler.syncToAll(serverPlayer);
        }
    }
}
