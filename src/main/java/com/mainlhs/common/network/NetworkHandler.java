package com.mainlhs.common.network;

import com.mainlhs.MainLHS;
import com.mainlhs.common.network.packet.SetRealisticModePacket;
import com.mainlhs.common.network.packet.SyncPlayerScalePacket;
import com.mainlhs.common.realistic.RealisticModeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MainLHS.MOD_ID, "sync"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId = 0;
    private static boolean registered;

    private NetworkHandler() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.registerMessage(
                packetId++,
                SyncPlayerScalePacket.class,
                SyncPlayerScalePacket::encode,
                SyncPlayerScalePacket::decode,
                SyncPlayerScalePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                SetRealisticModePacket.class,
                SetRealisticModePacket::encode,
                SetRealisticModePacket::decode,
                SetRealisticModePacket::handle
        );
    }

    public static void syncToAll(ServerPlayer player) {
        CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new SyncPlayerScalePacket(player.getUUID(), com.mainlhs.common.scale.PlayerScaleData.getSize(player))
        );
    }

    public static void syncToPlayer(ServerPlayer viewer, ServerPlayer subject) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> viewer),
                new SyncPlayerScalePacket(subject.getUUID(), com.mainlhs.common.scale.PlayerScaleData.getSize(subject))
        );
    }

    public static void syncRealisticToPlayer(ServerPlayer player) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SetRealisticModePacket(RealisticModeData.isEnabled(player))
        );
    }
}
