package net.jelly.sandworm_mod.event;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSign;
import net.jelly.sandworm_mod.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    // events that implement IModBusEvent are mod bus events
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
    }


    // REGISTER CAPABILITIES
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WormSign.class);
    }

    // creative mode tabs
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Add to ingredients tab
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WORM_TOOTH);
        }
    }





}
