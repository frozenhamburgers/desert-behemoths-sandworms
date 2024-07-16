package net.jelly.jelllymod.item;

import net.jelly.jelllymod.JellyMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems { // holds all items in mod
    // deferredregister is a big list of items that are registered as a certain time as forge loads the items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JellyMod.MODID);

    // add items
    public static final RegistryObject<Item> PARTICLE_TESTER = ITEMS.register("particle_tester", () -> new ParticleTestItem(new Item.Properties()));
    public static final RegistryObject<Item> WORM_TOOTH = ITEMS.register("sandworm_tooth", () -> new WormToothItem((new Item.Properties()).rarity(Rarity.UNCOMMON)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}
