package net.jelly.jelllymod;

import com.mojang.logging.LogUtils;
import net.jelly.jelllymod.entity.IK.ChainSegmentRenderer;
import net.jelly.jelllymod.entity.IK.KinematicChainRenderer;
import net.jelly.jelllymod.entity.IK.worm.WormHeadSegmentRenderer;
import net.jelly.jelllymod.entity.IK.worm.WormSegmentRenderer;
import net.jelly.jelllymod.entity.ModEntities;
import net.jelly.jelllymod.item.ModCreativeModeTabs;
import net.jelly.jelllymod.item.ModItems;
import net.jelly.jelllymod.networking.ModMessages;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(JellyMod.MODID)
public class JellyMod
{
    // Define mod id i n a common place for everything to reference
    public static final String MODID = "jelllymod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public JellyMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // registers items
        ModItems.register(modEventBus);
        // registers creative mode tabs
        ModCreativeModeTabs.register(modEventBus);

        //registers mobs
        ModEntities.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        // ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // networking
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            ModMessages.register(); // HAS TO BE FIRST LINE HERE
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
//            event.accept(ModItems.JELLY);
//            event.accept(ModItems.CUBEJELLY);
        }
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
        }
    }
}
