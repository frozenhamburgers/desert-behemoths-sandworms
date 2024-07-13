package net.jelly.jelllymod.event;

import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.capabilities.wormsign.WormSign;
import net.jelly.jelllymod.capabilities.wormsign.WormSignProvider;
import net.jelly.jelllymod.entity.ModEntities;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid= JellyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    // events that implement IModBusEvent are mod bus events
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
//        event.put(ModEntities.HOODIE.get(), HoodieEntity.createAttributes().build());
//        event.put(ModEntities.SHRIMP.get(), ShrimpEntity.createAttributes().build());
    }


    // REGISTER CAPABILITIES
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WormSign.class);
    }





}
