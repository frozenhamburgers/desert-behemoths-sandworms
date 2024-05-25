package net.jelly.jelllymod.entity;

import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.entity.IK.ChainSegment;
import net.jelly.jelllymod.entity.IK.worm.WormChainEntity;
import net.jelly.jelllymod.entity.IK.worm.WormHeadSegment;
import net.jelly.jelllymod.entity.IK.worm.WormSegment;
import net.jelly.jelllymod.entity.IK.KinematicChainEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JellyMod.MODID);

    public static final RegistryObject<EntityType<KinematicChainEntity>> KINEMATIC_CHAIN = ENTITY_TYPES.register("kinematic_chain", () ->
            EntityType.Builder.of(KinematicChainEntity::new, MobCategory.MISC)
                    .sized(1f,1f)
                    .build("kinematic_chain")
    );
    public static final RegistryObject<EntityType<ChainSegment>> CHAIN_SEGMENT = ENTITY_TYPES.register("chain_segment", () ->
            EntityType.Builder.of(ChainSegment::new, MobCategory.MISC)
                    .sized(0.5f,0.5f)
                    .build("chain_segment")
    );
    public static final RegistryObject<EntityType<WormSegment>> WORM_SEGMENT = ENTITY_TYPES.register("worm_segment", () ->
            EntityType.Builder.of(WormSegment::new, MobCategory.MISC)
                    .sized(7.5f,7.5f)
                    .clientTrackingRange(400)
                    .build("worm_segment")
    );
    public static final RegistryObject<EntityType<WormHeadSegment>> WORM_HEAD_SEGMENT = ENTITY_TYPES.register("worm_head_segment", () ->
            EntityType.Builder.of(WormHeadSegment::new, MobCategory.MISC)
                    .sized(7.5f,7.5f)
                    .clientTrackingRange(400)
                    .build("worm_head_segment")
    );
    public static final RegistryObject<EntityType<WormChainEntity>> WORM_CHAIN = ENTITY_TYPES.register("worm_chain", () ->
            EntityType.Builder.of(WormChainEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f)
                    .build("worm_chain")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
