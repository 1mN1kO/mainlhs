package com.mainlhs.client.screen;

import com.mainlhs.client.loading.LoadingVisuals;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntroLoadingScreen extends Screen {

    private float time;
    private float progress;
    private boolean switched;

    public IntroLoadingScreen() {
        super(Component.literal("Loading"));
    }

    @Override
    public void tick() {
        time += 0.05f;
        progress = Math.min(progress + 0.018f, 1.0f);

        if (!switched && progress >= 1.0f && this.minecraft != null) {
            switched = true;
            this.minecraft.setScreen(new MainMenuScreen(true));
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        float visualTime = time + partialTick * 0.05f;
        String status = MainMenuScreen.isRussian() ? "инициализация интерфейса" : "initializing interface";
        LoadingVisuals.renderFullScreen(gfx, this.font, this.width, this.height, visualTime, status, ease(progress));
    }

    private static float ease(float value) {
        float inverse = 1.0f - value;
        return 1.0f - inverse * inverse * inverse;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
