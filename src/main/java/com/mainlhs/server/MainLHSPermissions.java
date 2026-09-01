package com.mainlhs.server;

import com.mainlhs.MainLHS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@Mod.EventBusSubscriber(modid = MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MainLHSPermissions {

    public static PermissionNode<Boolean> GENERIC_SCALE_COMMAND;
    public static PermissionNode<Boolean> REALISTIC_COMMAND;

    private MainLHSPermissions() {}

    @SubscribeEvent
    public static void onPermissionGather(PermissionGatherEvent.Nodes event) {
        GENERIC_SCALE_COMMAND = new PermissionNode<>(
                MainLHS.MOD_ID,
                "command.genericscale",
                PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && player.hasPermissions(2)
        );
        event.addNodes(GENERIC_SCALE_COMMAND);

        REALISTIC_COMMAND = new PermissionNode<>(
                MainLHS.MOD_ID,
                "command.realistic",
                PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && player.hasPermissions(2)
        );
        event.addNodes(REALISTIC_COMMAND);
    }
}
