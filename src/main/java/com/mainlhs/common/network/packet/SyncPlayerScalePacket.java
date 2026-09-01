package com.mainlhs.common.network.packet;

import com.mainlhs.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SyncPlayerScalePacket(UUID playerId, float sizeParam) {

    public static void encode(SyncPlayerScalePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerId());
        buf.writeFloat(packet.sizeParam);
    }

    public static SyncPlayerScalePacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerScalePacket(buf.readUUID(), buf.readFloat());
    }

    public static void handle(SyncPlayerScalePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleScaleSync(packet)
        ));
        context.setPacketHandled(true);
    }
}
