package net.jelly.sandworm_mod.sound;

import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SandwormMod.MODID);

    public static final RegistryObject<SoundEvent> WORM_WARNING_1 = registerSoundEvents("worm_warning_1");
    public static final RegistryObject<SoundEvent> WORM_WARNING_2 = registerSoundEvents("worm_warning_2");
    public static final RegistryObject<SoundEvent> WORM_SPAWN = registerSoundEvents("worm_spawn");
    public static final RegistryObject<SoundEvent> WORM_BREACH = registerSoundEvents("worm_breach");
    public static final RegistryObject<SoundEvent> WORM_LAND = registerSoundEvents("worm_land");
    public static final RegistryObject<SoundEvent> WORM_BURROW = registerSoundEvents("worm_burrow");
    public static final RegistryObject<SoundEvent> WORM_ROAR = registerSoundEvents("worm_roar");
    public static final RegistryObject<SoundEvent> THUMPER = registerSoundEvents("thumper");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SandwormMod.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }


}
