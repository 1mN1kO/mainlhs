package com.mainlhs.server.command;

import com.mainlhs.common.network.NetworkHandler;
import com.mainlhs.common.realistic.RealisticModeData;
import com.mainlhs.server.MainLHSPermissions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;

public final class RealisticCommand {

    private RealisticCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("realistic")
                        .requires(RealisticCommand::hasPermission)
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> toggleRealistic(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                        )
        );
    }

    private static boolean hasPermission(CommandSourceStack source) {
        if (!source.isPlayer()) {
            return true;
        }

        try {
            ServerPlayer player = source.getPlayerOrException();
            if (MainLHSPermissions.REALISTIC_COMMAND == null) {
                return source.hasPermission(2);
            }
            return PermissionAPI.getPermission(player, MainLHSPermissions.REALISTIC_COMMAND);
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    private static int toggleRealistic(CommandSourceStack source, ServerPlayer target) {
        boolean enabled = RealisticModeData.toggle(target);
        NetworkHandler.syncRealisticToPlayer(target);

        source.sendSuccess(
                () -> Component.translatable(
                        enabled ? "commands.mainlhs.realistic.enabled" : "commands.mainlhs.realistic.disabled",
                        target.getDisplayName()
                ),
                true
        );
        return 1;
    }
}
