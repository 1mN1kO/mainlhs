package com.mainlhs.server;

import com.mainlhs.MainLHS;
import com.mainlhs.common.network.NetworkHandler;
import com.mainlhs.common.realistic.RealisticModeData;
import com.mainlhs.common.scale.PlayerScaleData;
import com.mainlhs.common.scale.ScaleMath;
import com.mainlhs.server.command.GenericScaleCommand;
import com.mainlhs.server.command.RealisticCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MainLHS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ScaleEvents {

    private ScaleEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GenericScaleCommand.register(event.getDispatcher());
        RealisticCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player) || !player.isAddedToWorld()) {
            return;
        }

        float sizeParam = PlayerScaleData.getSize(player);
        if (Math.abs(sizeParam - 1.0f) < 0.0001f) {
            return;
        }

        float factor = ScaleMath.getScaleFactor(sizeParam);
        EntityDimensions baseSize = getBasePlayerDimensions(event.getPose());
        EntityDimensions scaled = baseSize.scale(factor, factor);
        event.setNewSize(scaled, true);
        
        float baseEyeHeight = player.getStandingEyeHeight(event.getPose(), baseSize);
        event.setNewEyeHeight(baseEyeHeight * factor);
    }

    private static EntityDimensions getBasePlayerDimensions(Pose pose) {
        return switch (pose) {
            case CROUCHING -> EntityDimensions.scalable(Player.STANDING_DIMENSIONS.width, Player.CROUCH_BB_HEIGHT);
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> EntityDimensions.scalable(Player.SWIMMING_BB_WIDTH, Player.SWIMMING_BB_HEIGHT);
            default -> Player.STANDING_DIMENSIONS;
        };
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        float size = PlayerScaleData.getSize(event.getOriginal());
        if (Math.abs(size - 1.0f) < 0.0001f) {
            RealisticModeData.setEnabled(event.getEntity(), RealisticModeData.isEnabled(event.getOriginal()));
            return;
        }
        PlayerScaleData.setSize(event.getEntity(), size);
        RealisticModeData.setEnabled(event.getEntity(), RealisticModeData.isEnabled(event.getOriginal()));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        NetworkHandler.syncToAll(player);
        NetworkHandler.syncRealisticToPlayer(player);

        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other != player) {
                NetworkHandler.syncToPlayer(player, other);
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer tracker) {
            NetworkHandler.syncToPlayer(tracker, target);
        }
    }
}
