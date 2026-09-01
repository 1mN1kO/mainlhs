package com.mainlhs.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientSoundHelper {

    private ClientSoundHelper() {}

    public static void playHoverSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        try {
            float master = mc.options.getSoundSourceVolume(SoundSource.MASTER);
            if (master > 0.001f) {
                mc.getSoundManager().play(
                        new SimpleSoundInstance(
                                ModSounds.BUTTON_HOVER.get().getLocation(),
                                SoundSource.MASTER,
                                1.0f,
                                1.0f,
                                SoundInstance.createUnseededRandom(),
                                false,
                                0,
                                SoundInstance.Attenuation.NONE,
                                0.0, 0.0, 0.0,
                                true
                        )
                );
            }
        } catch (Throwable ignored) {}
    }

    public static void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        try {
            float master = mc.options.getSoundSourceVolume(SoundSource.MASTER);
            if (master > 0.001f) {
                mc.getSoundManager().play(
                        new SimpleSoundInstance(
                                ModSounds.BUTTON_CLICK.get().getLocation(),
                                SoundSource.MASTER,
                                1.0f,
                                1.0f,
                                SoundInstance.createUnseededRandom(),
                                false,
                                0,
                                SoundInstance.Attenuation.NONE,
                                0.0, 0.0, 0.0,
                                true
                        )
                );
            }
        } catch (Throwable ignored) {}
    }
}
