package com.mainlhs.client.loading;

import com.mainlhs.client.screen.MainMenuScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.function.Consumer;

public class BrandedLoadingOverlay extends LoadingOverlay {

    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private boolean completed;

    public BrandedLoadingOverlay(
            Minecraft minecraft,
            ReloadInstance reload,
            Consumer<Optional<Throwable>> onFinish,
            boolean fadeIn
    ) {
        super(minecraft, reload, onFinish, fadeIn);
        this.minecraft = minecraft;
        this.reload = reload;
        this.onFinish = onFinish;
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = Mth.clamp(currentProgress, 0.0f, 1.0f);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        currentProgress = Mth.clamp(currentProgress * 0.86f + reload.getActualProgress() * 0.14f, 0.0f, 1.0f);

        float fade = 1.0f;
        if (fadeOutStart >= 0L) {
            fade = 1.0f - Mth.clamp((now - fadeOutStart) / 280.0f, 0.0f, 1.0f);
        }

        String status = MainMenuScreen.isRussian() ? "подготовка ресурсов" : "preparing resources";
        LoadingVisuals.renderFullScreen(gfx, minecraft.font, minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight(), now / 1000.0f, status, currentProgress);

        if (fade < 1.0f) {
            gfx.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(),
                    ((int) ((1.0f - fade) * 255.0f) << 24));
        }

        if (fadeOutStart < 0L && reload.isDone() && currentProgress >= 0.995f) {
            fadeOutStart = now;
        }

        if (!completed && fadeOutStart >= 0L && now - fadeOutStart > 280L) {
            completed = true;
            try {
                reload.checkExceptions();
                onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                onFinish.accept(Optional.of(throwable));
            }

            if (minecraft.getOverlay() == this) {
                minecraft.setOverlay(null);
            }
        }
    }
}
