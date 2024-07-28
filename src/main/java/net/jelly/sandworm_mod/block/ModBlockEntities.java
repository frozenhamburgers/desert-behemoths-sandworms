package net.jelly.sandworm_mod.block;

import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SandwormMod.MODID);

    public static final RegistryObject<BlockEntityType<ThumperBlockEntity>> THUMPER_ENTITY =
            BLOCK_ENTITIES.register("animated_block_entity", () ->
                    BlockEntityType.Builder.of(ThumperBlockEntity::new,
                            ModBlocks.THUMPER.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
