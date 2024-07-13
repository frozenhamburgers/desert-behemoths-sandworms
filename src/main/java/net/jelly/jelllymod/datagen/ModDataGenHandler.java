package net.jelly.jelllymod.datagen;

import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.registry.common.DamageTypesRegistry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = JellyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenHandler {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, DamageTypesRegistry::bootstrap);

    @SubscribeEvent
    public static void gatherDataEvent(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        dataGenerator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                packOutput, event.getLookupProvider(), BUILDER, Set.of(JellyMod.MODID)));
    }
}
