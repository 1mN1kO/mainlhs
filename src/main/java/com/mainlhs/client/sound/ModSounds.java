package com.mainlhs.client.sound;

import com.mainlhs.MainLHS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MainLHS.MOD_ID);

    public static final RegistryObject<SoundEvent> BUTTON_HOVER = register("ui.btn_hover");
    public static final RegistryObject<SoundEvent> BUTTON_CLICK = register("ui.btn_click");
    public static final RegistryObject<SoundEvent> MENU_BGM = register("music.menu_bgm");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MainLHS.MOD_ID, name)));
    }

    public static void playHoverSound() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSoundHelper::playHoverSound);
    }

    public static void playClickSound() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSoundHelper::playClickSound);
    }
}
