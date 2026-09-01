package com.mainlhs.server.command;

import com.mainlhs.common.scale.PlayerScaleData;
import com.mainlhs.common.scale.ScaleMath;
import com.mainlhs.server.MainLHSPermissions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;

public final class GenericScaleCommand {

    private GenericScaleCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("genericscale")
                        .requires(GenericScaleCommand::hasPermission)
                        .then(
                                Commands.argument("target", EntityArgument.player())
                                        .then(
                                                Commands.argument("size", FloatArgumentType.floatArg(0.1f, 64.0f))
                                                        .executes(ctx -> setScale(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                FloatArgumentType.getFloat(ctx, "size")
                                                        ))
                                        )
                        )
        );
    }

    private static boolean hasPermission(CommandSourceStack source) {
        if (!source.isPlayer()) {
            return true;
        }

        try {
            ServerPlayer player = source.getPlayerOrException();
            if (MainLHSPermissions.GENERIC_SCALE_COMMAND == null) {
                return source.hasPermission(2);
            }
            return PermissionAPI.getPermission(player, MainLHSPermissions.GENERIC_SCALE_COMMAND);
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    private static int setScale(CommandSourceStack source, ServerPlayer target, float size) {
        PlayerScaleData.setSize(target, size);

        float height = ScaleMath.heightFromSizeParam(size);
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.mainlhs.genericscale.success",
                        target.getDisplayName(),
                        String.format("%.2f", size),
                        String.format("%.2f", height)
                ),
                true
        );
        return 1;
    }
}
