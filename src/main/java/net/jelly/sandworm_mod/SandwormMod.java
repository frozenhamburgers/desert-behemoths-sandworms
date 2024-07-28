package net.jelly.sandworm_mod;

import com.mojang.logging.LogUtils;
import net.jelly.sandworm_mod.block.ModBlockEntities;
import net.jelly.sandworm_mod.block.ModBlocks;
import net.jelly.sandworm_mod.block.thumper.ThumperRenderer;
import net.jelly.sandworm_mod.brewing.WormToothBrewing;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.ChainSegmentRenderer;
import net.jelly.sandworm_mod.entity.IK.KinematicChainRenderer;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegmentRenderer;
import net.jelly.sandworm_mod.entity.IK.worm.WormSegmentRenderer;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.item.ModItems;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.jelly.sandworm_mod.registry.client.ParticleRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SandwormMod.MODID)
public class SandwormMod
{
    // Define mod id i n a common place for everything to reference
    public static final String MODID = "sandworm_mod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public SandwormMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // register
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        // registers creative mode tabs
        // ModCreativeModeTabs.register(modEventBus);

        //registers mobs
        ModEntities.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // register sounds
        ModSounds.register(modEventBus);

        // register particles
        ParticleRegistry.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, "sandwormmod-common.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    // common setup
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            // ModMessages.register(); // networking: HAS TO BE FIRST LINE HERE
            BrewingRecipeRegistry.addRecipe(new WormToothBrewing());
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // register entity renderers
            EntityRenderers.register(ModEntities.KINEMATIC_CHAIN.get(), KinematicChainRenderer::new);
            EntityRenderers.register(ModEntities.CHAIN_SEGMENT.get(), ChainSegmentRenderer::new);
            EntityRenderers.register(ModEntities.WORM_SEGMENT.get(), WormSegmentRenderer::new);
            EntityRenderers.register(ModEntities.WORM_HEAD_SEGMENT.get(), WormHeadSegmentRenderer::new);
            EntityRenderers.register(ModEntities.WORM_CHAIN.get(), KinematicChainRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.THUMPER_ENTITY.get(), ThumperRenderer::new);
        }
    }
}
