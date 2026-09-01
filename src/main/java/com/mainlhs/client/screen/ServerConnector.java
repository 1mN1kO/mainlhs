package com.mainlhs.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ServerConnector {

    private static final String SERVER_HOST = "144.31.136.75";
    private static final int SERVER_PORT = 25566;
    private static final String SERVER_ADDR = SERVER_HOST + ":" + SERVER_PORT;

    private ServerConnector() {}

    public static String getServerAddress() {
        return SERVER_ADDR;
    }

    /**
     * Connect to the default configured server address.
     */
    public static void connect(Screen parent) {
        connect(parent, SERVER_ADDR);
    }

    /**
     * Connect to a custom address in the form host:port or host.
     */
    public static void connect(Screen parent, String address) {
        if (address == null || address.isBlank()) return;
        String addr = address.trim();
        ServerData serverData = new ServerData(
                "Last Human Stronghold",
                addr,
                false
        );
        try {
            ConnectScreen.startConnecting(
                    parent,
                    Minecraft.getInstance(),
                    ServerAddress.parseString(addr),
                    serverData,
                    false
            );
        } catch (Exception e) {
            // Fallback: attempt to parse host-only address by appending default port
            try {
                String withPort = addr.contains(":" ) ? addr : addr + ":" + SERVER_PORT;
                ConnectScreen.startConnecting(parent, Minecraft.getInstance(), ServerAddress.parseString(withPort), serverData, false);
            } catch (Exception ex) {
                // ignore - connection will fail and Minecraft will show the error screen
            }
        }
    }
}
