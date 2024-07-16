package net.jelly.sandworm_mod.registry.client;

import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.lodestar.lodestone.systems.particle.world.type.LodestoneWorldParticleType;

public class ParticleRegistry {
    public static DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SandwormMod.MODID);
    public static RegistryObject<LodestoneWorldParticleType> CRINGE_PARTICLE = PARTICLES.register("cringe", LodestoneWorldParticleType::new);

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }

    public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(CRINGE_PARTICLE.get(), LodestoneWorldParticleType.Factory::new);
    }

}