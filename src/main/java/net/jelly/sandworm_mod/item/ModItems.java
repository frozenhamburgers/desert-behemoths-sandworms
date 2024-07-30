package net.jelly.sandworm_mod.item;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.block.ModBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems { // holds all items in mod
    // deferredregister is a big list of items that are registered as a certain time as forge loads the items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SandwormMod.MODID);

    // add items
    public static final RegistryObject<Item> WORM_TOOTH = ITEMS.register("sandworm_tooth", () -> new WormToothItem((new Item.Properties()).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THUMPER_ITEM = ITEMS.register("thumper", () -> new ThumperItem(ModBlocks.THUMPER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}
