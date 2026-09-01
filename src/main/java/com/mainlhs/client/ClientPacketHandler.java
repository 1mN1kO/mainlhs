package com.mainlhs.client;

import com.mainlhs.common.network.packet.SyncPlayerScalePacket;
import com.mainlhs.common.network.packet.SetRealisticModePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientPacketHandler {

    private ClientPacketHandler() {}

    public static void handleScaleSync(SyncPlayerScalePacket packet) {
        ClientScaleCache.set(packet.playerId(), packet.sizeParam());

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Player player = minecraft.level.getPlayerByUUID(packet.playerId());
        if (player != null) {
            player.refreshDimensions();
        }
    }

    public static void handleRealisticMode(SetRealisticModePacket packet) {
        ClientRealisticMode.setEnabled(packet.enabled());
    }

    public static float getPlayerSize(Player player) {
        return ClientScaleCache.get(player.getUUID());
    }
}
