package com.mainlhs.common.network.packet;

import com.mainlhs.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetRealisticModePacket(boolean enabled) {

    public static void encode(SetRealisticModePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.enabled());
    }

    public static SetRealisticModePacket decode(FriendlyByteBuf buf) {
        return new SetRealisticModePacket(buf.readBoolean());
    }

    public static void handle(SetRealisticModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleRealisticMode(packet)
        ));
        context.setPacketHandled(true);
    }
}
