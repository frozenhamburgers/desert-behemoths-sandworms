package net.jelly.jelllymod.item;

import net.jelly.jelllymod.JellyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JellyMod.MODID);

//    public static final RegistryObject<CreativeModeTab> JELLY_TAB = CREATIVE_MODE_TABS.register("jelly_tab", () -> CreativeModeTab.builder()
//            .icon(() -> new ItemStack(ModItems.JELLY.get()))                // icon
//            .title(Component.translatable("creativetab.jelly_tab"))   // name
//            .displayItems((pParameters, pOutput) -> {                       // items in tab
//                pOutput.accept(ModItems.JELLY.get());
//                pOutput.accept(ModItems.CUBEJELLY.get());
//            })
//            .build());                                                      // build the builder
    public static void register(IEventBus eventbus) {
//        CREATIVE_MODE_TABS.register(eventbus);
    }
}
